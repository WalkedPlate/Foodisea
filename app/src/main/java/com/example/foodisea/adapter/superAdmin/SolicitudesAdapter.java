package com.example.foodisea.adapter.superAdmin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodisea.R;
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

        if (!usuario.getFoto().isEmpty()) {
            int imageResId = context.getResources().getIdentifier(usuario.getFoto(), "drawable", context.getPackageName());
            if (imageResId != 0) {
                holder.binding.ivUserPhoto.setImageResource(imageResId);
            } else {
                holder.binding.ivUserPhoto.setImageResource(R.drawable.ic_usuarios); // Imagen por defecto si no se encuentra
            }
        } else {
            holder.binding.ivUserPhoto.setImageResource(R.drawable.ic_usuarios); // Imagen por defecto si no hay imágenes
        }

        // Opcional: si quieres agregar un onClickListener al ítem de usuario
        holder.itemView.setOnClickListener(v -> {
            // Acción al hacer clic en el usuario (puedes abrir otra actividad o mostrar más detalles)
        });
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
}
