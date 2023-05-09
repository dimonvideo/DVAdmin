package dv.dimonvideo.dvadmin.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dv.dimonvideo.dvadmin.R;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private final List<String> mData;
    private final List<String> mCount;
    private final LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    int selectedPos = RecyclerView.NO_POSITION;

    public Adapter(Context context, List<String> data, List<String> count) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mCount = count;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String title = mData.get(position);
        holder.myTextView.setText(title);
        String count = mCount.get(position);
        holder.myCountView.setText(count);

        holder.itemView.setBackgroundColor(selectedPos == position ? Color.GREEN : Color.TRANSPARENT);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;
        TextView myCountView;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.Name);
            myCountView = itemView.findViewById(R.id.Value);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                if (getAdapterPosition() == RecyclerView.NO_POSITION) return;
                notifyItemChanged(selectedPos);
                selectedPos = getAdapterPosition();
                notifyItemChanged(selectedPos);
                mClickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    public String getItem(int id) {
        return mData.get(id);
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}