package com.example.foodisea.adapter.superAdmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.R;

import com.example.foodisea.databinding.ItemRestaurantBinding;
import com.example.foodisea.databinding.ItemUserBinding;
import com.example.foodisea.model.Usuario;

import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    private Context context;
    private List<Usuario> usuarioList;

    public UsuarioAdapter(Context context, List<Usuario> usuarioList) {
        this.context = context;
        this.usuarioList = usuarioList;
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

        // Configurar el MaterialSwitch según el estado del usuario
        holder.binding.swUserActive.setChecked(usuario.getEstado().equals("Activo"));

        // Listener para cambios en el estado del switch
        holder.binding.swUserActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Actualizar el estado del usuario
            usuario.setEstado(isChecked ? "Activo" : "Inactivo");
            // Aquí puedes añadir la lógica para actualizar el estado en la base de datos o backend
        });

        // Opcional: si quieres agregar un onClickListener al ítem de usuario
        holder.itemView.setOnClickListener(v -> {
            // Acción al hacer clic en el usuario (puedes abrir otra actividad o mostrar más detalles)
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
