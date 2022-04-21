package com.example.VisualTour.Percorsi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.VisualTour.CourseModel;
import com.example.VisualTour.R;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class CourseAdapterPercorsi extends RecyclerView.Adapter<CourseAdapterPercorsi.Viewholder> {

    private Context context;
    private List<CourseModel> list;
    private  ItemClickListener clickListener;
    public static ArrayList<String> aggiunto=new ArrayList<>();
    public static boolean visualizzazione=false;
     // Constructor
    public CourseAdapterPercorsi(List<CourseModel> list, ItemClickListener clickListener) {
        this.list = list;
        this.clickListener  = clickListener;

    }



    @NonNull
    @Override
    public Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate the layout for each item of recycler view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardperc, parent, false);

    return new Viewholder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull Viewholder holder, int position) {
        // to set data to textview and imageview of each card layout
        CourseModel model = list.get(position);
        holder.Nome.setText(model.getCourse_name());
        holder.Tipo.setText("" + model.getCourse_rating());
        holder.ID.setText(model.getID());
        if(visualizzazione){
            holder.add.setVisibility(View.INVISIBLE);
            holder.remove.setVisibility(View.INVISIBLE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    clickListener.Details(list.get(position), (String) holder.ID.getText());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        holder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    boolean presente=false;
                    for (int i=0; i<= aggiunto.size(); i++){
                        if(i>=1){
                            if(holder.Nome.getText().equals(aggiunto.get(i-1))){
                                presente=true;
                                break;
                            }
                        }
                    }
                    if(!presente){
                        aggiunto.add((String) holder.Nome.getText());//add disattivato
                        clickListener.Add(list.get(position), (String) holder.Nome.getText());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    boolean presente=false;
                    for (int i=0; i<= aggiunto.size(); i++){
                        if(i >=1){
                            if(holder.Nome.getText().equals(aggiunto.get(i-1))){
                                presente=true;
                                break;
                            }
                        }
                    }
                    if(presente){
                        aggiunto.remove((String) holder.Nome.getText());
                        clickListener.Remove(list.get(position), (String) holder.Nome.getText());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        // this method is used for showing number
        // of card items in recycler view.
        return list.size();
    }

    public static class Viewholder extends RecyclerView.ViewHolder {
        private TextView Nome;
        private TextView Tipo;
        public Button add;
        public Button remove;
        public TextView ID;
        RelativeLayout relativeLayout;

        public Viewholder(@NonNull View itemView ){
            super(itemView);
            Nome = itemView.findViewById(R.id.NomePerc);
            Tipo = itemView.findViewById(R.id.Tipo);
            add = itemView.findViewById(R.id.add);
            remove = itemView.findViewById(R.id.remove);
            relativeLayout = itemView.findViewById(R.id.idRVCourse);
            ID = itemView.findViewById(R.id.ID);

        }
    }
    public interface  ItemClickListener {
        public void Add(CourseModel dataModel,String id) throws JSONException;
        public void Remove(CourseModel dataModel,String id) throws JSONException;
        public void Details(CourseModel dataModel,String id) throws JSONException;

    }
}