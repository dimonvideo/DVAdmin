/**
 * Утилитный класс для отображения диалогового окна с индикатором прогресса в приложении DVAdmin.
 * Предоставляет методы для показа, проверки видимости и скрытия диалога с настраиваемым
 * сообщением.
 */
package dv.dimonvideo.dvadmin.util;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

/**
 * Управляет созданием и отображением диалогового окна с прогресс-баром.
 */
public class ProgressHelper {
    /** Экземпляр диалогового окна, отображаемого в данный момент. */
    private static AlertDialog dialog = null;

    /**
     * Показывает диалоговое окно с индикатором прогресса и указанным сообщением. Если диалог уже
     * открыт, новый не создаётся.
     *
     * @param context Контекст приложения.
     * @param message Сообщение, отображаемое рядом с прогресс-баром.
     */
    public static void showDialog(Context context, String message) {
        if (dialog == null) {
            int llPadding = 30;
            LinearLayout ll = new LinearLayout(context);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setPadding(llPadding, llPadding, llPadding, llPadding);
            ll.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            llParam.gravity = Gravity.CENTER;
            ll.setLayoutParams(llParam);

            ProgressBar progressBar = new ProgressBar(context);
            progressBar.setIndeterminate(true);
            progressBar.setPadding(0, 0, llPadding, 0);
            progressBar.setLayoutParams(llParam);

            llParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            llParam.gravity = Gravity.CENTER;
            TextView tvText = new TextView(context);
            tvText.setText(message);
            tvText.setTextColor(Color.parseColor("#000000"));
            tvText.setTextSize(20);
            tvText.setLayoutParams(llParam);

            ll.addView(progressBar);
            ll.addView(tvText);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(true);
            builder.setView(ll);

            dialog = builder.create();
            dialog.show();
            Window window = dialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(dialog.getWindow().getAttributes());
                layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                dialog.getWindow().setAttributes(layoutParams);
            }
        }
    }

    /**
     * Проверяет, отображается ли диалоговое окно с прогресс-баром.
     *
     * @return true, если диалог видим, иначе false.
     */
    public static boolean isDialogVisible() {
        if (dialog != null) {
            return dialog.isShowing();
        } else {
            return false;
        }
    }

    /**
     * Скрывает диалоговое окно с прогресс-баром и освобождает ресурсы.
     */
    public static void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }
}