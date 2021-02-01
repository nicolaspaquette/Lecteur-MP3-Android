package nicolas.paquette.travail1_lecteurmp3;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Vector;

public class ListeArtistes extends AppCompatActivity {

    ListView listeArtistes;
    Vector<String> artistesPossibles;
    Ecouteur ecouteur;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liste_artistes);

        listeArtistes = findViewById(R.id.listeDesArtistes);
        ecouteur = new Ecouteur();

        artistesPossibles = EnsembleChansons.getInstance().retournerArtistes(this);
        ArrayAdapter aa = new ArrayAdapter(this, R.layout.row, artistesPossibles);
        listeArtistes.setAdapter(aa);
        listeArtistes.setOnItemClickListener(ecouteur);
    }

    private class Ecouteur implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> adapterView, View element, int i, long l) {
            String nom = artistesPossibles.get(i);

            Intent intent = new Intent(getApplicationContext(), ListeChansons.class);
            intent.putExtra("nomArtiste", nom); // Extra pour savoir si on veut la liste d'un artiste ou toutes les chansons
            startActivity(intent);
        }
    }
}