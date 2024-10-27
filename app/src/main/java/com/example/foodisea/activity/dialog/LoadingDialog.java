package com.example.foodisea.activity.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.foodisea.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Diálogo personalizado para mostrar un indicador de carga con mensaje personalizable
 */
public class LoadingDialog {
    private final Dialog dialog;
    private final TextView tvMessage;

    /**
     * Constructor del diálogo de carga
     * @param context Contexto de la aplicación
     */
    public LoadingDialog(Context context) {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.setCancelable(false);

        if (dialog.getWindow() != null) {
            // Fondo semitransparente
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_dim_background);

            // Configurar el diálogo para que ocupe toda la pantalla
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;

            // Asegurar que el diálogo esté centrado
            lp.gravity = Gravity.CENTER;

            dialog.getWindow().setAttributes(lp);
        }

        tvMessage = dialog.findViewById(R.id.tvMessage);
    }

    /**
     * Muestra el diálogo de carga con el mensaje por defecto
     */
    public void show() {
        show(null);
    }

    /**
     * Muestra el diálogo de carga con un mensaje personalizado
     * @param message Mensaje a mostrar, si es null se usa el mensaje por defecto
     */
    public void show(String message) {
        if (!dialog.isShowing()) {
            if (message != null) {
                tvMessage.setText(message);
            }
            dialog.show();
        }
    }

    /**
     * Oculta el diálogo de carga
     */
    public void dismiss() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public boolean isShowing() {
        return dialog.isShowing();
    }
}