package nicolas.paquette.travail1_lecteurmp3;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Size;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public class EnsembleChansons {
    private Vector<Chanson> listeChansons;
    private int index; // index de la chanson a lire dans le vecteur
    private ContentResolver resolver;

    // sigleton
    private static EnsembleChansons instance;

    public static EnsembleChansons getInstance(){
        if (instance == null)
            instance = new EnsembleChansons();
        return instance;
    }

    private EnsembleChansons(){
        listeChansons = new Vector<Chanson>();
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public Vector<String> retournerArtistes(Activity a){ // retourne le nom de tous les artistes
        resolver = a.getContentResolver();
        Uri artistUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Vector<String> resultat = new Vector<String>();
        String temp;

        Cursor artistCursor = resolver.query(artistUri, new String[]{"artist"}, null, null, "artist");

        while(artistCursor.moveToNext()){
            temp = artistCursor.getString(0);
            resultat.add(temp);
        }

        artistCursor.close();
        return resultat;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public Vector<Hashtable<String, Object>> retournerToutesLesChansons(Activity a){ // retourne toutes les chansons, peut importe l'artiste
        resolver = a.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri imageUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

        Vector<Hashtable<String, Object>> resultat = new Vector<Hashtable<String, Object>>();
        Chanson temp;
        Bitmap pochette = null;

        Cursor musicCursor = resolver.query(musicUri, new String[]{"_id", "album", "artist", "title", "duration"}, null, null, "title");

        while(musicCursor.moveToNext()){
            //chanson
            temp = new Chanson ( (int) musicCursor.getLong(0), null, musicCursor.getString(1), musicCursor.getString(2), musicCursor.getString(3), musicCursor.getInt(4));
            //pochette
            Cursor pochetteCursor = resolver.query(imageUri, new String[]{"album_id", "album"}, "album=?", new String[]{musicCursor.getString(1)}, null);

            if (pochetteCursor != null && pochetteCursor.moveToFirst()){
                long id_de_album = pochetteCursor.getLong(0);
                Uri contentUri = ContentUris.withAppendedId(imageUri, id_de_album);

                try{
                    pochette = resolver.loadThumbnail(contentUri, new Size(640, 480), null);
                }
                catch(IOException e){
                    e.printStackTrace();
                }
                temp.setPochette(pochette);
                pochetteCursor.close();
            }

            Hashtable<String, Object> informationChanson = new Hashtable<String, Object>(); // hashtable pour les informations de la chanson
            informationChanson.put("id", temp.getId());
            informationChanson.put("titre", temp.getTitre());
            informationChanson.put("artiste", temp.getArtiste());
            informationChanson.put("pochette", temp.getPochette());
            informationChanson.put("duration", temp.getDuree());

            resultat.add(informationChanson); // on ajoute le hashtable au vecteur de hashtables
        }
        musicCursor.close();
        return resultat;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public Vector<Hashtable<String, Object>> retournerChansonsArtiste(Activity a, String nom){ // on retourne toutes chansons de l'artiste spécifique
        resolver = a.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri imageUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

        Vector<Hashtable<String, Object>> resultat = new Vector<Hashtable<String, Object>>();
        Chanson temp;
        Bitmap pochette = null;

        Cursor musicCursor = resolver.query(musicUri, new String[]{"_id", "album", "artist", "title", "duration"}, "artist=?", new String[]{nom}, "title"); // selon l'artiste en paramètre

        while(musicCursor.moveToNext()){

            //chanson COLONNES SELON L'ORDRE DES PARAMETRES DANS LE CONSTRUCTEUR CHANSON
            temp = new Chanson ( (int) musicCursor.getLong(0), null, musicCursor.getString(1), musicCursor.getString(2), musicCursor.getString(3), musicCursor.getInt(4));
            //pochette
            Cursor pochetteCursor = resolver.query(imageUri, new String[]{"album_id", "album"}, "album=?", new String[]{musicCursor.getString(1)}, null);

            if (pochetteCursor != null && pochetteCursor.moveToFirst()){
                long id_de_album = pochetteCursor.getLong(0);
                Uri contentUri = ContentUris.withAppendedId(imageUri, id_de_album);

                try{
                    pochette = resolver.loadThumbnail(contentUri, new Size(640, 480), null);
                }
                catch(IOException e){
                    e.printStackTrace();
                }
                temp.setPochette(pochette);
                pochetteCursor.close();
            }

            Hashtable<String, Object> informationChanson = new Hashtable<String, Object>();
            informationChanson.put("id", temp.getId());
            informationChanson.put("titre", temp.getTitre());
            informationChanson.put("artiste", temp.getArtiste());
            informationChanson.put("pochette", temp.getPochette());
            informationChanson.put("duration", temp.getDuree());

            resultat.add(informationChanson);
        }
        musicCursor.close();
        return resultat;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public Hashtable<String, Object> afficherChanson(Activity a, String titre, String artiste){ // trouver une chanson spécifique
        resolver = a.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri imageUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;

        Hashtable<String, Object> informationChanson = new Hashtable<String, Object>();
        Chanson temp;
        Bitmap pochette = null;

        Cursor musicCursor = resolver.query(musicUri, new String[]{"_id", "album", "artist", "title", "duration"}, "title=? AND artist=?", new String[]{titre, artiste}, null); // selon le titre et l'artiste

        while(musicCursor.moveToNext()){

            //chanson COLONNES SELON L'ORDRE DES PARAMETRES DANS LE CONSTRUCTEUR CHANSON
            temp = new Chanson ( (int) musicCursor.getLong(0), null, musicCursor.getString(1), musicCursor.getString(2), musicCursor.getString(3), musicCursor.getInt(4));
            //pochette
            Cursor pochetteCursor = resolver.query(imageUri, new String[]{"album_id", "album"}, "album=?", new String[]{musicCursor.getString(1)}, null);

            if (pochetteCursor != null && pochetteCursor.moveToFirst()){
                long id_de_album = pochetteCursor.getLong(0);
                Uri contentUri = ContentUris.withAppendedId(imageUri, id_de_album);

                try{
                    pochette = resolver.loadThumbnail(contentUri, new Size(640, 480), null);
                }
                catch(IOException e){
                    e.printStackTrace();
                }
                temp.setPochette(pochette);
                pochetteCursor.close();
            }
            informationChanson.put("id", temp.getId());
            informationChanson.put("titre", temp.getTitre());
            informationChanson.put("artiste", temp.getArtiste());
            informationChanson.put("pochette", temp.getPochette());
            informationChanson.put("duration", temp.getDuree());
        }
        musicCursor.close();
        return informationChanson;
    }
}
