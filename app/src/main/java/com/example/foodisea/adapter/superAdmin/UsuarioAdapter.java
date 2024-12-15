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
import com.example.foodisea.model.Usuario;
import com.example.foodisea.repository.UsuarioRepository;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private final UsuarioRepository usuarioRepository;
    private final Context context;
    private final List<Usuario> usuarioList;

    public UsuarioAdapter(Context context, List<Usuario> usuarioList, UsuarioRepository usuarioRepository) {
        this.context = context;
        this.usuarioList = usuarioList;
        this.usuarioRepository = usuarioRepository; // Inyección de dependencia
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
        // Obtener el usuario actual
        Usuario usuario = usuarioList.get(position);

        // Establecer el nombre completo y correo
        String nombreCompleto = usuario.getNombres() + " " + usuario.getApellidos();
        holder.binding.tvUserName.setText(nombreCompleto);
        holder.binding.tvCorreo.setText(usuario.getCorreo());

        // Cargar la imagen usando Glide
        if (!usuario.getFoto().isEmpty()) {
            Glide.with(context)
                    .load(usuario.getFoto())  // URL o ruta local de la imagen
                    .placeholder(R.drawable.ic_usuarios)  // Imagen por defecto
                    .error(R.drawable.ic_usuarios)  // Imagen en caso de error
                    .circleCrop()  // Imagen redondeada
                    .into(holder.binding.ivUserPhoto);
        } else {
            holder.binding.ivUserPhoto.setImageResource(R.drawable.ic_usuarios);  // Imagen por defecto si no hay foto
        }

        // Configurar el MaterialSwitch según el estado del usuario
        holder.binding.swUserActive.setChecked(usuario.getEstado().equals("Activo"));

        // Listener para cambios en el estado del switch
        holder.binding.swUserActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Actualizar el estado del usuario
            usuario.setEstado(isChecked ? "Activo" : "Inactivo");
            // Actualizar el estado en la base de datos o backend
            usuarioRepository.actualizarEstadoUsuario(usuario.getId(), usuario.getEstado())
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        // Opcional: si quieres agregar un onClickListener al ítem de usuario
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SuperAdminDetalleUsuarioActivity.class);
            intent.putExtra("usuario", usuario); // Enviar usuario completo
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return usuarioList.size();
    }

    public static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        ItemUserBinding binding;

        public UsuarioViewHolder(ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
