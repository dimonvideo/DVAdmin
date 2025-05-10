/**
 * Утилитный класс для управления запросами разрешений во время выполнения в приложении DVAdmin.
 * Предоставляет методы для проверки и запроса разрешений, а также обработки результатов запросов
 * через слушатель {@link RequestPermissionListener}.
 */
package dv.dimonvideo.dvadmin.util;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Упрощает процесс запроса разрешений, фильтруя уже предоставленные и уведомляя о результатах.
 */
public class RequestPermissionHandler {
    /** Активность, из которой запрашиваются разрешения. */
    private Activity mActivity;

    /** Слушатель результатов запроса разрешений. */
    private RequestPermissionListener mRequestPermissionListener;

    /** Код запроса разрешений. */
    private int mRequestCode;

    /**
     * Запрашивает указанные разрешения, проверяя их необходимость и вызывая соответствующие
     * методы слушателя в зависимости от результата.
     *
     * @param activity    Активность, из которой выполняется запрос.
     * @param permissions Массив запрашиваемых разрешений.
     * @param requestCode Код запроса для обработки результата.
     * @param listener    Слушатель результатов запроса.
     */
    public void requestPermission(Activity activity, @NonNull String[] permissions, int requestCode,
                                  RequestPermissionListener listener) {
        mActivity = activity;
        mRequestCode = requestCode;
        mRequestPermissionListener = listener;

        if (!needRequestRuntimePermissions()) {
            mRequestPermissionListener.onSuccess();
            return;
        }
        requestUnGrantedPermissions(permissions, requestCode);
    }

    /**
     * Проверяет необходимость запроса разрешений во время выполнения.
     *
     * @return true, если запрос разрешений требуется, иначе false.
     */
    private boolean needRequestRuntimePermissions() {
        return true;
    }

    /**
     * Запрашивает разрешения, которые ещё не предоставлены.
     *
     * @param permissions Массив запрашиваемых разрешений.
     * @param requestCode Код запроса для обработки результата.
     */
    private void requestUnGrantedPermissions(String[] permissions, int requestCode) {
        String[] unGrantedPermissions = findUnGrantedPermissions(permissions);
        if (unGrantedPermissions.length == 0) {
            mRequestPermissionListener.onSuccess();
            return;
        }
        ActivityCompat.requestPermissions(mActivity, unGrantedPermissions, requestCode);
    }

    /**
     * Проверяет, предоставлено ли указанное разрешение.
     *
     * @param permission Разрешение для проверки.
     * @return true, если разрешение предоставлено, иначе false.
     */
    private boolean isPermissionGranted(String permission) {
        return ActivityCompat.checkSelfPermission(mActivity, permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Находит неподтверждённые разрешения из переданного массива.
     *
     * @param permissions Массив разрешений для проверки.
     * @return Массив неподтверждённых разрешений.
     */
    private String[] findUnGrantedPermissions(String[] permissions) {
        List<String> unGrantedPermissionList = new ArrayList<>();
        for (String permission : permissions) {
            if (!isPermissionGranted(permission)) {
                unGrantedPermissionList.add(permission);
            }
        }
        return unGrantedPermissionList.toArray(new String[0]);
    }

    /**
     * Обрабатывает результат запроса разрешений, уведомляя слушатель о успехе или неудаче.
     *
     * @param requestCode  Код запроса.
     * @param permissions  Массив запрошенных разрешений.
     * @param grantResults Массив результатов запроса.
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == mRequestCode) {
            if (grantResults.length > 0) {
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        mRequestPermissionListener.onFailed();
                        return;
                    }
                }
                mRequestPermissionListener.onSuccess();
            } else {
                mRequestPermissionListener.onFailed();
            }
        }
    }

    /**
     * Интерфейс для обработки результатов запроса разрешений.
     */
    public interface RequestPermissionListener {
        /**
         * Вызывается при успешном предоставлении всех разрешений.
         */
        void onSuccess();

        /**
         * Вызывается при отказе в предоставлении хотя бы одного разрешения.
         */
        void onFailed();
    }
}