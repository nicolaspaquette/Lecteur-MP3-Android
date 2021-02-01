package nicolas.paquette.travail1_lecteurmp3;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;

import java.util.Hashtable;
import java.util.Vector;

public class ListeChansons extends AppCompatActivity {

    ListView listeChansons;
    Vector<Hashtable<String, Object>> ChansonsPossibles;
    Ecouteur ecouteur;
    String nom;
    CheckBox lectureAleatoire;
    CheckboxEcouteur checkboxEcouteur;
    Boolean aleatoire = false;
    String nomArtiste;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_chansons);

        listeChansons = findViewById(R.id.listeDesChansons);
        ecouteur = new Ecouteur();

        lectureAleatoire = findViewById(R.id.lectureAleatoire);
        checkboxEcouteur = new CheckboxEcouteur();
        lectureAleatoire.setOnCheckedChangeListener(checkboxEcouteur);

        Intent intent = getIntent();
        nomArtiste = intent.getStringExtra("nomArtiste");
        if (intent.getStringExtra("nomArtiste").equals("TousLesArtistes")){ // si on passe par le MainActivity (bouton Chansons)
            ChansonsPossibles = EnsembleChansons.getInstance().retournerToutesLesChansons(this); // la liste de chansons est donc toutes les chansons disponibles
        }
        else {
            String nom = intent.getStringExtra("nomArtiste"); // si on passe par le ListeArtistes (bouton Artistes)
            ChansonsPossibles = EnsembleChansons.getInstance().retournerChansonsArtiste(this, nom); // la liste de chansons est seulement les chansons de l'artiste spécifique
        }

        SimpleAdapter adapter = new SimpleAdapter(this, ChansonsPossibles, R.layout.simplelist,
                new String[]{"titre", "artiste", "pochette"},
                new int[]{R.id.listeTitre,R.id.listeArtiste,R.id.listePochette});

        adapter.setViewBinder(new SimpleAdapter.ViewBinder(){

            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if( (view instanceof ImageView) & (data instanceof Bitmap) ) {
                    ImageView iv = (ImageView) view;
                    Bitmap bm = (Bitmap) data;
                    iv.setImageBitmap(bm);
                    return true;
                }
                return false;
            }
        });

        listeChansons.setAdapter(adapter);
        listeChansons.setOnItemClickListener(ecouteur);
    }

    private class Ecouteur implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> adapterView, View element, int i, long l) {
            Intent intent = new Intent(getApplicationContext(), LecteurChansons.class); // lorsqu'on clique, on envoie à LecteurChansons les informations nécessaires pour faire la requête de la chanson
            intent.putExtra("titre", (String) ChansonsPossibles.get(i).get("titre"));
            intent.putExtra("artiste", (String) ChansonsPossibles.get(i).get("artiste"));
            intent.putExtra("aleatoire", aleatoire);
            intent.putExtra("nomArtiste", nomArtiste);
            startActivity(intent);
        }
    }

    private class CheckboxEcouteur implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) { // on envoie aussi en extra le Boolean afin de savoir si la lecture est aléatoire ou non
            if (b)
                aleatoire = true;
            else
                aleatoire = false;
        }
    }
}