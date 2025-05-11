/**
 * Адаптер для {@link RecyclerView} в приложении DVAdmin, отображающий категории модерации и
 * количество материалов, ожидающих проверки. Поддерживает выделение выбранного элемента и
 * обработку нажатий для перехода к соответствующим журналам модерации.
 */
package dv.dimonvideo.dvadmin.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dv.dimonvideo.dvadmin.R;

/**
 * Наследует {@link RecyclerView.Adapter} для управления списком категорий модерации и их
 * количеств.
 */
public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    /** Список названий категорий модерации. */
    private final List<String> mData;

    /** Список количеств материалов в категориях. */
    private final List<String> mCount;

    /** Объект для создания представлений из макета. */
    private final LayoutInflater mInflater;

    /** Слушатель событий нажатий на элементы списка. */
    private ItemClickListener mClickListener;

    /** Позиция выделенного элемента (-1, если ничего не выбрано). */
    private int selectedPos = RecyclerView.NO_POSITION;

    /**
     * Конструктор адаптера, инициализирующий данные и контекст.
     *
     * @param context Контекст приложения.
     * @param data    Список названий категорий модерации.
     * @param count   Список количеств материалов в категориях.
     */
    public Adapter(Context context, List<String> data, List<String> count) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mCount = count;
    }

    /**
     * Создаёт новый объект {@link ViewHolder} для элемента списка.
     *
     * @param parent   Родительский контейнер.
     * @param viewType Тип представления.
     * @return Новый объект {@link ViewHolder}.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Привязывает данные к представлению элемента списка, устанавливая название, количество и
     * выделение.
     *
     * @param holder   Объект {@link ViewHolder} для элемента.
     * @param position Позиция элемента в списке.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String title = mData.get(position);
        holder.myTextView.setText(title);
        String count = mCount.get(position);
        holder.myCountView.setText(count);

        holder.itemView.setBackgroundColor(selectedPos == position ? Color.GREEN : Color.TRANSPARENT);
    }

    /**
     * Возвращает количество элементов в списке.
     *
     * @return Размер списка категорий.
     */
    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * Класс ViewHolder для хранения представлений элемента списка.
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        /** TextView для отображения названия категории. */
        TextView myTextView;

        /** TextView для отображения количества материалов. */
        TextView myCountView;

        /**
         * Конструктор, инициализирующий представления и слушатель нажатий.
         *
         * @param itemView Представление элемента списка.
         */
        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.Name);
            myCountView = itemView.findViewById(R.id.Value);
            itemView.setOnClickListener(this);
        }

        /**
         * Обрабатывает нажатие на элемент списка, обновляя выделение и вызывая слушатель.
         *
         * @param view Нажатый элемент.
         */
        @Override
        public void onClick(View view) {
            Log.d("DVAdminApp", "Нажатие на элемент RecyclerView, позиция: " + getAdapterPosition());
            if (mClickListener != null) {
                if (getAdapterPosition() == RecyclerView.NO_POSITION) {
                    Log.w("DVAdminApp", "Позиция недействительна");
                    return;
                }
                notifyItemChanged(selectedPos);
                selectedPos = getAdapterPosition();
                notifyItemChanged(selectedPos);
                Log.d("DVAdminApp", "Вызываем onItemClick для позиции: " + selectedPos);
                mClickListener.onItemClick(view, getAdapterPosition());
            } else {
                Log.w("DVAdminApp", "Слушатель нажатий (mClickListener) не установлен");
            }
        }
    }

    /**
     * Возвращает название категории по указанной позиции.
     *
     * @param id Позиция в списке.
     * @return Название категории.
     */
    public String getItem(int id) {
        return mData.get(id);
    }

    /**
     * Устанавливает слушатель событий нажатий на элементы списка.
     *
     * @param itemClickListener Слушатель событий.
     */
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
        Log.d("DVAdminApp", "Слушатель нажатий установлен");
    }

    /**
     * Интерфейс для обработки нажатий на элементы списка.
     */
    public interface ItemClickListener {
        /**
         * Вызывается при нажатии на элемент списка.
         *
         * @param view     Нажатый элемент.
         * @param position Позиция элемента в списке.
         */
        void onItemClick(View view, int position);
    }
}