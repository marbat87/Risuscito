package it.cammino.risuscito;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by marcello.battain on 12/01/2015.
 */
public class CantoRecyclerAdapter extends RecyclerView.Adapter {

    private List<CantoItem> dataItems;
    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;

    // Adapter constructor
    public CantoRecyclerAdapter(List<CantoItem> dataItems
            , View.OnClickListener clickListener
            , View.OnLongClickListener longClickListener) {

        this.dataItems = dataItems;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        View layoutView = LayoutInflater
                .from(viewGroup.getContext())
                .inflate(R.layout.row_item, null);
        return new CantoViewHolder(layoutView, clickListener, longClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        CantoItem dataItem = dataItems.get(position);

        // Casting the viewHolder to MyViewHolder so I could interact with the views
        CantoViewHolder cantoHolder = (CantoViewHolder) viewHolder;
        cantoHolder.cantoTitle.setText(dataItem.getTitolo());
        cantoHolder.cantoPage.setText(dataItem.getPagina());
        if (dataItem.getColore().equalsIgnoreCase(Utility.GIALLO))
            cantoHolder.cantoPage.setBackgroundResource(R.drawable.bkg_round_yellow);
        if (dataItem.getColore().equalsIgnoreCase(Utility.GRIGIO))
            cantoHolder.cantoPage.setBackgroundResource(R.drawable.bkg_round_grey);
        if (dataItem.getColore().equalsIgnoreCase(Utility.VERDE))
            cantoHolder.cantoPage.setBackgroundResource(R.drawable.bkg_round_green);
        if (dataItem.getColore().equalsIgnoreCase(Utility.AZZURRO))
            cantoHolder.cantoPage.setBackgroundResource(R.drawable.bkg_round_blue);
        if (dataItem.getColore().equalsIgnoreCase(Utility.BIANCO))
            cantoHolder.cantoPage.setBackgroundResource(R.drawable.bkg_round_white);
    }

    @Override
    public int getItemCount() {
        return dataItems.size();
    }

    public static class CantoViewHolder extends RecyclerView.ViewHolder {

        public TextView cantoTitle;
        public TextView cantoPage;


        public CantoViewHolder(View itemView, View.OnClickListener clickListener, View.OnLongClickListener onLongClickListener) {
            super(itemView);
            cantoTitle = (TextView) itemView.findViewById(R.id.text_title);
            cantoPage = (TextView) itemView.findViewById(R.id.text_page);
            itemView.setOnClickListener(clickListener);
            itemView.setOnLongClickListener(onLongClickListener);
        }



    }
}
