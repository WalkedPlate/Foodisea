package com.example.foodisea.adapter.superAdmin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodisea.R;

import com.example.foodisea.activity.superadmin.SuperAdminDetalleUsuarioActivity;
import com.example.foodisea.databinding.ItemUserBinding;
import com.example.foodisea.manager.LogManager;
import com.example.foodisea.model.Restaurante;
import com.example.foodisea.model.Usuario;
import com.example.foodisea.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private final UsuarioRepository usuarioRepository;
    private final Context context;
    private final List<Usuario> usuarioList;
    private List<Usuario> usuariosFiltrados;
    private final LogManager logManager;

    public UsuarioAdapter(Context context, List<Usuario> usuarioList, UsuarioRepository usuarioRepository, LogManager logManager) {
        this.context = context;
        this.usuarioList = usuarioList;
        this.usuarioRepository = usuarioRepository; // Inyección de dependencia
        this.usuariosFiltrados= new ArrayList<>(usuarioList); // Inicializar con la lista original
        this.logManager = logManager;
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemUserBinding binding = ItemUserBinding.inflate(inflater, parent, false);
        return new UsuarioViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = usuariosFiltrados.get(position);

        String nombreCompleto = usuario.getNombres() + " " + usuario.getApellidos();
        holder.binding.tvUserName.setText(nombreCompleto);
        holder.binding.tvCorreo.setText(usuario.getCorreo());

        if (!usuario.getFoto().isEmpty()) {
            Glide.with(context)
                    .load(usuario.getFoto())
                    .placeholder(R.drawable.ic_usuarios)
                    .error(R.drawable.ic_usuarios)
                    .circleCrop()
                    .into(holder.binding.ivUserPhoto);
        } else {
            holder.binding.ivUserPhoto.setImageResource(R.drawable.ic_usuarios);
        }

        // Eliminar cualquier listener anterior
        holder.binding.swUserActive.setOnCheckedChangeListener(null);

        // Configurar el estado del switch sin activar el listener
        holder.binding.swUserActive.setChecked(usuario.getEstado().equals("Activo"));

        // Reconfigurar el listener para cambios posteriores
        holder.binding.swUserActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            usuario.setEstado(isChecked ? "Activo" : "Inactivo");
            usuarioRepository.actualizarEstadoUsuario(usuario.getId(), usuario.getEstado())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show();
                        String accion = isChecked ? "Activado" : "Desactivado";
                        String detalles = "Usuario: " + nombreCompleto + " - ha sido: " + accion;
                        logManager.createLog(usuario.getId(), accion, detalles)
                                .addOnSuccessListener(documentReference -> Toast.makeText(context, "Log registrado", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(context, "Error al registrar log: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SuperAdminDetalleUsuarioActivity.class);
            intent.putExtra("usuario", usuario);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return usuariosFiltrados.size();
    }

    // Método para filtrar la lista
    public void filter(String query) {
        usuariosFiltrados.clear();
        if (query.isEmpty()) {
            usuariosFiltrados.addAll(usuarioList); // Mostrar todos si el texto está vacío
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Usuario usuario : usuarioList) {
                if (usuario.getNombres().toLowerCase().contains(lowerCaseQuery)) {
                    usuariosFiltrados.add(usuario);
                }
            }
        }
        notifyDataSetChanged(); // Notificar cambios al adaptador
    }

    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        ItemUserBinding binding;

        public UsuarioViewHolder(ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
