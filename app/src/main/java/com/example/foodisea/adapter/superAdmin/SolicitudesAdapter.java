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
import com.example.foodisea.activity.superadmin.SuperAdminDetSoliRepartidorActivity;
import com.example.foodisea.databinding.ItemSoliRepartidorBinding;
import com.example.foodisea.model.Usuario;
import com.example.foodisea.repository.UsuarioRepository;

import java.util.List;

public class SolicitudesAdapter extends RecyclerView.Adapter<SolicitudesAdapter.SolicitudViewHolder> {

    private final UsuarioRepository usuarioRepository;
    private final Context context;
    private final List<Usuario> usuarioSoliList;

    public SolicitudesAdapter(Context context, List<Usuario> usuarioSoliList, UsuarioRepository usuarioRepository) {
        this.context = context;
        this.usuarioSoliList = usuarioSoliList;
        this.usuarioRepository = usuarioRepository; // Inyección de dependencia
    }

    @NonNull
    @Override
    public SolicitudViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemSoliRepartidorBinding binding = ItemSoliRepartidorBinding.inflate(inflater, parent, false);
        return new SolicitudViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SolicitudViewHolder holder, int position) {
        // Obtener el usuario actual
        Usuario usuario = usuarioSoliList.get(position);

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

        // Navegar a SuperAdminDetSoliRepartidorActivity al hacer clic en el ítem
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SuperAdminDetSoliRepartidorActivity.class);
            intent.putExtra("usuario", usuario); // Enviar usuario completo
            context.startActivity(intent);
        });

        // Actualizar estado con btnAccept
        holder.binding.btnAccept.setOnClickListener(v -> actualizarEstado(usuario, "Activo",position));

        // Actualizar estado con btnDeny
        holder.binding.btnDeny.setOnClickListener(v -> actualizarEstado(usuario, "Denegado",position));
    }

    @Override
    public int getItemCount() {
        return usuarioSoliList.size();
    }

    public static class SolicitudViewHolder extends RecyclerView.ViewHolder {
        ItemSoliRepartidorBinding binding;

        public SolicitudViewHolder(ItemSoliRepartidorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    // Método para actualizar estado
    private void actualizarEstado(Usuario usuario, String nuevoEstado, int position) {
        usuario.setEstado(nuevoEstado);

        usuarioRepository.actualizarEstadoUsuario(usuario, nuevoEstado)
                .addOnSuccessListener(aVoid -> {
                    if(nuevoEstado.equals("Activo")){
                        Toast.makeText(context, "El usuario ha sido aceptado" , Toast.LENGTH_SHORT).show();

                    } else if (nuevoEstado.equals("Denegado")) {
                        Toast.makeText(context, "El usuario ha sido denegado", Toast.LENGTH_SHORT).show();
                    }

                    // Eliminar usuario de la lista
                    usuarioSoliList.remove(position);
                    notifyItemRemoved(position);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
