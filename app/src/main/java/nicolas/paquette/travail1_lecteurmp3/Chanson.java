package nicolas.paquette.travail1_lecteurmp3;

import android.graphics.Bitmap;

public class Chanson {
    private String titre, album, artiste;
    private int id;
    private int duree;
    private Bitmap pochette;

    public Chanson(int id, Bitmap pochette, String album, String artiste, String titre, int duree){
        this.titre = titre;
        this.album = album;
        this.artiste = artiste;
        this.id = id;
        this.duree = duree;
    }

    public String getTitre() {
        return titre;
    }

    public String getAlbum() {
        return album;
    }

    public String getArtiste() {
        return artiste;
    }

    public int getId() {
        return id;
    }

    public int getDuree() {
        return duree;
    }

    public Bitmap getPochette() {
        return pochette;
    }

    public void setPochette(Bitmap pochette) {
        this.pochette = pochette;
    }
}
