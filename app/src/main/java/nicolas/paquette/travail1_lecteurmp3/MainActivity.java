package nicolas.paquette.travail1_lecteurmp3;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Hashtable;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    Button buttonArtistes, buttonChansons;
    Ecouteur ecouteur;
    TextView derniereChanson;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonArtistes = findViewById(R.id.buttonArtistes);
        buttonChansons = findViewById(R.id.buttonChansons);
        ecouteur = new Ecouteur();

        derniereChanson = findViewById(R.id.derniereChansonEcoutee);

        buttonArtistes.setOnClickListener(ecouteur);
        buttonChansons.setOnClickListener(ecouteur);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //depuis l'API 23
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { //si ce n'est pas déjà permis, on fait la demande explicite
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 555);
            }
        }

        try{
            Hashtable<String, Object> chanson = derniereChansonEcoutee(); // on regarde dans le fichier sérializable (fichier.ser)
            derniereChanson.setText("Dernière chanson écoutée: " + chanson.get("titre") + " par " + chanson.get("artiste")); // on affiche le titre et l'artiste
        }
        catch (Exception e){
            Log.i("exception", e.toString());
            derniereChanson.setText("Pas de dernière chanson écoutée");
        }
    }

    public Hashtable<String, Object> derniereChansonEcoutee() throws Exception{ // la fonction qui recherche le fichier "fichier.ser"
        Hashtable<String, Object> temp = null;
        FileInputStream fis = openFileInput("fichier.ser");
        ObjectInputStream ois = new ObjectInputStream(fis);

        temp = (Hashtable<String, Object>) ois.readObject(); // retourne une hashtable contenant les informations sur la chanson
        ois.close();
        return temp;
    }

    private class Ecouteur implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            if (view == buttonArtistes){
                Intent intent = new Intent(getApplicationContext(),ListeArtistes.class); // on se dirige vers la page des artistes
                startActivity(intent);
            }
            else if (view == buttonChansons){
                Intent intent = new Intent(getApplicationContext(),ListeChansons.class); // on se dirige vers la page de toutes les chansons
                intent.putExtra("nomArtiste", "TousLesArtistes");
                startActivity(intent);
            }
        }
    }

    @Override
    protected void onSaveInstanceState (Bundle saveInstanceState){ // avant le onStop
        super.onSaveInstanceState(saveInstanceState);

    }

    @Override
    public void onRequestPermissionsResult ( int requestCode, String[]permissions, int [] grantResults){
        if(requestCode==555)
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED) // vient du popup sur le téléphone
                Toast.makeText(this,"Permission accordée ! ",Toast.LENGTH_LONG).show();
    }
}