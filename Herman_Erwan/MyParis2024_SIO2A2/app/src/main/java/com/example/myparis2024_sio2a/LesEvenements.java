package com.example.myparis2024_sio2a;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class LesEvenements extends AppCompatActivity implements View.OnClickListener {
    private Button btRetour ;
    private ListView lvListe ;
    private static ArrayList<Evenement> LesEvents = null;

    public  static void setLesEvents (ArrayList<Evenement> lesEvents)
    {
        LesEvenements.LesEvents = lesEvents ;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_les_evenements);

        this.btRetour=(Button) findViewById(R.id.idRetour);
        this.lvListe=(ListView) findViewById(R.id.idListe);
        this.btRetour.setOnClickListener(this);

        LesEvenements ev = this;
        Thread unT = new Thread(new Runnable() {
            @Override
            public void run() {
                EventsBDD lesEventsBDD = new EventsBDD();
               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       //Extraction des evenements

                       lesEventsBDD.execute ();

                       //remplir la liste View
                       ArrayList<String> lesEventsChaine = new ArrayList<>();
                       if(LesEvents !=null) {
                           for (Evenement unEvent : LesEvents) {
                               lesEventsChaine.add(unEvent.getDesignation() + "  "
                                       + unEvent.getDateEvent() + "  " + unEvent.getLieu());
                           }
                           ArrayAdapter unAdapter = new ArrayAdapter(ev,
                                   android.R.layout.simple_list_item_1, lesEventsChaine);
                           lvListe.setAdapter(unAdapter);
                       } //fin du if
                   }
               });
            }
        });
        unT.start();


    }

    @Override
    public void onClick(View v) {
        String email = this.getIntent().getStringExtra("email").toString();
        if(v.getId() == R.id.idRetour){
            Intent unIntent = new Intent(this, Menu.class);
            unIntent.putExtra("email", email);
            this.startActivity(unIntent);
        }
    }
}

////////////// Connexion ?? la BDD ////////////////////
class EventsBDD extends AsyncTask<Void, Void, ArrayList<Evenement>> {

    @Override
    protected ArrayList<Evenement> doInBackground(Void... voids) {
        String resultatJson ="";
        ArrayList<Evenement> lesEvents = new ArrayList<>();

        String url = "http://localhost/Promotion_241/Projets/Herman_Erwan/androidParis2024/getLesEvenements.php";
        try{
            URL uneUrl = new URL(url);
            HttpURLConnection uneConnexion = (HttpURLConnection)uneUrl.openConnection();
            //on fixe la m??thode d'envoi des donn??es
            uneConnexion.setRequestMethod("GET");
            //ouverture d'envoi et r??ception des donn??ees
            uneConnexion.setDoInput(true);
            uneConnexion.setDoOutput(true);
            //on fixe le temps d'attente de la connexion
            uneConnexion.setConnectTimeout(15000);
            //on se connecte
            uneConnexion.connect();

            //lecture des donn??es JSON
            //d??claration d'un fichier de lecture
            InputStreamReader isr =new InputStreamReader(uneConnexion.getInputStream(), "UTF-8");
            //lire dans un buffer : chaine
            BufferedReader br = new BufferedReader(isr);
            //extraction des lignes de la page PHP
            String ligne ="";
            StringBuilder sb = new StringBuilder();//chaine dynamique
            while ((ligne=br.readLine())!=null){
                sb.append(ligne); //tant qu'il un JSon, on le lit et on le concatene
            }
            //construction du json r??sultat
            resultatJson = sb.toString();
            Log.e("JSON :", resultatJson);
        }
        catch (Exception exp){
            Log.e("Erreur:", "Connexion Impossible a "+ url);
            exp.printStackTrace();
        }
        //parsing du JSON
        try{
            JSONArray tab = new JSONArray(resultatJson); //tableau de JSON []
            //extraction d'un seul JSON resultat
            for(int i =0; i <tab.length(); i++) {
                JSONObject unObjet = tab.getJSONObject(i);
                Evenement e = new Evenement(
                        unObjet.getInt("idevenement"),
                        unObjet.getInt("nbplaces"),(float) unObjet.getDouble("prix"),
                        unObjet.getString("designation"), unObjet.getString("dateevent"),
                        unObjet.getString("heureevent"), unObjet.getString("lieu")
                );
                lesEvents.add(e);
            }
        }
        catch(JSONException exp ){
            Log.e("JSON ERREUR: ", "Impossible de parser le json");
            exp.printStackTrace();
        }

        return lesEvents;
    }

    @Override
    protected void onPostExecute(ArrayList<Evenement> lesEvents) {
        // on r??cup??re apr??s ex??cution de la t??che asynchrone
        LesEvenements.setLesEvents(lesEvents);
    }
}