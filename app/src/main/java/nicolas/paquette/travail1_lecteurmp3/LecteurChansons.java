package nicolas.paquette.travail1_lecteurmp3;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

public class LecteurChansons extends AppCompatActivity {

    ImageView pochette, last, play, next;
    TextView titreChanson, dureeChanson;
    Boolean aleatoire;
    String titre, artiste;
    MusicService musicSrv;
    MediaPlayer mediaPlayer;
    GestionDiffuseur gd;
    Boolean musicBound;
    MusiqueConnection connection;
    Intent playIntent;
    Hashtable<String, Object> chanson;
    Ecouteur ecouteur;
    Boolean jouerChanson = true, nouvelleChanson = true;
    Vector<Hashtable<String, Object>> ChansonsPossibles;
    Vector<Hashtable<String, Object>> ordreChansons;
    SeekBar seekbarChanson;
    Chronometer tempsChanson;
    Long tempsArret;
    Integer ancienneValeurSeekbar = 0;
    String nomArtiste;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lecteur_chansons);

        ChansonsPossibles = EnsembleChansons.getInstance().retournerToutesLesChansons(this);
        ChansonsPossibles.trimToSize();

        ordreChansons = new Vector<Hashtable<String, Object>>();

        pochette = findViewById(R.id.pochette);
        last = findViewById(R.id.last);
        play = findViewById(R.id.play);
        next = findViewById(R.id.next);
        titreChanson = findViewById(R.id.titreChanson);
        tempsChanson = findViewById(R.id.tempsChanson);
        dureeChanson = findViewById(R.id.dureeChanson);
        seekbarChanson = findViewById(R.id.seekBarChanson);

        Intent intent = getIntent(); // on reçoit les informations de l'activité précédente
        aleatoire = intent.getBooleanExtra("aleatoire", false);
        titre = intent.getStringExtra("titre");
        artiste = intent.getStringExtra("artiste");
        nomArtiste = intent.getStringExtra("nomArtiste");

        if (nomArtiste.equals("TousLesArtistes")) // si on arrive de l'activité ayant toutes les chansons, la liste de chansons disponible sera égale à toutes les chansons
            ChansonsPossibles = EnsembleChansons.getInstance().retournerToutesLesChansons(this);
        else
            ChansonsPossibles = EnsembleChansons.getInstance().retournerChansonsArtiste(this, artiste); // si nous avons cliqué sur un artiste spécifique, seulement les chansons de cet artistes seront disponibles

        chanson = EnsembleChansons.getInstance().afficherChanson(this, titre, artiste); // on trouve la chanson que l'on veut écouter

        // on écrit les informations relatives à la chanson dans les bons endroits
        titreChanson.setText((String) chanson.get("titre"));

        Integer valeurDuree = (Integer) chanson.get("duration");
        long duree = valeurDuree;
        long minutes = (duree / 1000) / 60;
        long secondes = (duree / 1000) % 60;
        String temps = String.format("%02d:%02d", minutes, secondes);
        dureeChanson.setText(temps);

        pochette.setImageBitmap((Bitmap) chanson.get("pochette"));

        ecouteur = new Ecouteur();
        play.setOnClickListener(ecouteur);
        last.setOnClickListener(ecouteur);
        next.setOnClickListener(ecouteur);

        tempsChanson.setOnChronometerTickListener(ecouteur);
        seekbarChanson.setOnSeekBarChangeListener(ecouteur);
    }

    @Override
    protected void onStart() {
        super.onStart();
        playIntent = new Intent(getApplicationContext(), MusicService.class);
        connection = new MusiqueConnection();
        bindService(playIntent, connection, Context.BIND_AUTO_CREATE);
    }

    private class MusiqueConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicSrv = binder.getService(); //permet d’initialiser variable déclaréee
            mediaPlayer = musicSrv.getDiffuseur(); // permet d’initialiser variable déclarée

            gd = new GestionDiffuseur();
            mediaPlayer.setOnPreparedListener(gd);
            mediaPlayer.setOnCompletionListener(gd);
            mediaPlayer.setOnErrorListener(gd);
            mediaPlayer.setOnSeekCompleteListener(gd);

            musicBound = true;

            if (nouvelleChanson) { // pour que la chanson s'active lorsqu'on commence l'activité (sans avoir à peser sur PLAY)
                musicSrv.choisirChanson(0, (Integer) chanson.get("id"));
                nouvelleChanson = false;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound=false;
        }
    }

    private class GestionDiffuseur implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener{

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            prochaineChanson();
        }

        @Override
        public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
            return false;
        }

        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mediaPlayer.start(); // on commence la chanson
            tempsChanson.start(); // on commence le chronomètre
            sauvegarderDerniereChanson(); // on enregistre dans le fichier.ser le nom de la chanson
        }

        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {

        }
    }

    private class Ecouteur implements View.OnClickListener, Chronometer.OnChronometerTickListener, SeekBar.OnSeekBarChangeListener{

        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public void onClick(View view) {
            if (view == play){
                if (!jouerChanson) {
                    play.setImageResource(android.R.drawable.ic_media_pause); // on change l'image pour pause
                    jouerChanson = true;
                    mediaPlayer.start();

                    tempsChanson.setBase(SystemClock.elapsedRealtime() + tempsArret); // pour que le chronomètre soit au bon temps
                    tempsChanson.start();

                }
                else {
                    play.setImageResource(android.R.drawable.ic_media_play); // on change l'image pour play
                    jouerChanson = false;
                    mediaPlayer.pause();

                    tempsArret = tempsChanson.getBase() - SystemClock.elapsedRealtime(); // pour que le chronomètre soit au bon temps lorsqu'on reprend la chanson
                    tempsChanson.stop();
                }

            }
            else if (view == last){
                if (!ordreChansons.isEmpty()){ // si il y a plus d'une chanson de jouée
                    int index = ordreChansons.size() - 1;
                    Hashtable<String, Object> derniereChanson = ordreChansons.get(index); // on va prendre la derniere chanson jouée avant la chanson qui joue en ce moment
                    ordreChansons.remove(index); // on l'enlève des anciennes chansons (car elle va devenir la chanson qui joue)

                    modifierChanson(derniereChanson); // fonction pour changer la chanson actuelle
                    chanson = derniereChanson; // la dernière chanson jouée devient la chanson actuelle

                    tempsChanson.setBase(SystemClock.elapsedRealtime()); // on recommence le temps de la chanson
                    tempsChanson.stop();
                }
            }
            else if (view == next){
                prochaineChanson(); // on change la chanson selon la liste de chansons possibles
            }
        }

        @Override
        public void onChronometerTick(Chronometer chronometer) {
            String temps [] = (String.valueOf(chronometer.getText())).split(":");
            float tempsChanson = (((Integer.parseInt(temps[0])) * 60) + (Integer.parseInt(temps[1]))) * 1000; // obtenir les millisecondes
            float tempsTotal = (Integer) chanson.get("duration");

            float progression =  (tempsChanson / tempsTotal) * 100; // la pourcentage de la seekbar dépends du temps actuel et du temps total de la chanson
            seekbarChanson.setProgress(Math.round(progression));
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            float tempsAdditionnel;
            float tempsVoulu = seekBar.getProgress();
            float dureeTotal = (Integer) chanson.get("duration");
            float millisecondes = dureeTotal * (tempsVoulu / 100); // la valeur de la seekbar correspond au pourcentage de la chanson

            ancienneValeurSeekbar = Math.round(tempsVoulu);

            musicSrv.cherche(Math.round(millisecondes)); // on mets la chanson au temps en millisecondes équivalent au pourcentage de la chanson que l'on désire

            String temps [] = (String.valueOf(tempsChanson.getText())).split(":");
            float tempsChrono = (((Integer.parseInt(temps[0])) * 60) + (Integer.parseInt(temps[1]))) * 1000; // obtenir les millisecondes
            tempsAdditionnel = millisecondes - tempsChrono; // on avance ou recule le chronomètre afin d'obtenir le bon temps pour la chanson

            //  AJUSTER LE CHRONO EN CONSÉQUENCE
            tempsChanson.setBase(tempsChanson.getBase() - (long) tempsAdditionnel);
        }
    }

    public void prochaineChanson(){
        Hashtable<String, Object> prochaineChanson = chanson;

        if (aleatoire){ // si on a choisi la lecture aléatoire, l'index dans la liste sera choisi au hasard
            while (prochaineChanson.equals(chanson) && ChansonsPossibles.size() > 1){ // tant qu'on ne trouve pas une nouvelle chanson, on continue à chercher
                Random rand = new Random();
                int random = rand.nextInt(ChansonsPossibles.size());

                prochaineChanson = ChansonsPossibles.get(random); // la prochaine chanson est choisie parmis la liste de choix possibles
            }
        }
        else{
            int index = 0;
            int taille = ChansonsPossibles.size();
            for (int i = 0; i < taille; i++){
                if (ChansonsPossibles.get(i).equals(chanson)){ // on trouve l'index dans la chanson à l'écoute actuellement
                    index = i;
                }
            }
            if (index == taille - 1) // si c'est la dernière chanson de la liste, on revient au début
                index = 0;
            else
                index++;

            prochaineChanson = ChansonsPossibles.get(index); // la prochaine chanson est donc la suivante (ou la première) dans la liste de chansons possibles
        }

        tempsChanson.setBase(SystemClock.elapsedRealtime()); // on recommence le chronomètre
        tempsChanson.stop();

        modifierChanson(prochaineChanson); // on effectue la méthode qui change les informations dans l'application relative à la chanson

        ordreChansons.add(chanson); // la dernière chanson jouée entre dans l'ordre des chansons (afin de pouvoir reculer dans la liste et écouter les chansons précédentes)
        chanson = prochaineChanson; // la nouvelle chanson actuelle est définie
    }

    public void modifierChanson(Hashtable<String, Object> chanson){ // on modifie à l'écran les informations concernant la chanson qui joue
        titreChanson.setText((String) chanson.get("titre"));

        Integer valeurDuree = (Integer) chanson.get("duration");
        long duree = valeurDuree;
        long minutes = (duree / 1000) / 60;
        long secondes = (duree / 1000) % 60;
        String temps = String.format("%02d:%02d", minutes, secondes);
        dureeChanson.setText(temps);

        pochette.setImageBitmap((Bitmap) chanson.get("pochette"));

        musicSrv.choisirChanson(0, (Integer) chanson.get("id"));

        play.setImageResource(android.R.drawable.ic_media_pause);
        jouerChanson = true;

        tempsChanson.start(); // on commence l'écoute après avoir tout changer les informations

        sauvegarderDerniereChanson(); // on sauvegarde les informations de la chanson comme dernière chanson jouée
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopService(playIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    public void sauvegarderDerniereChanson(){
        Hashtable<String, Object> derniereChanson = new Hashtable<String, Object>(); // on emmagasine le titre et la nom de l'artiste lorsqu'on change de chanson
        derniereChanson.put("titre", chanson.get("titre"));
        derniereChanson.put("artiste", chanson.get("artiste"));

        try{
            FileOutputStream fos = openFileOutput("fichier.ser", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(derniereChanson);
            oos.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}














