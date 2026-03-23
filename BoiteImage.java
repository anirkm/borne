import MG2D.geometrie.Point;
import MG2D.geometrie.Rectangle;
import MG2D.geometrie.Texture;
import java.io.File;


public class BoiteImage extends Boite{

    Texture image;

    BoiteImage(Rectangle rectangle, String image) {
	super(rectangle);
	this.image = new Texture(resolveImagePath(image), new Point(760, 648));
    }

    public Texture getImage() {
	return this.image;
    }

    public void setImage(String chemin) {
	this.image.setImg(resolveImagePath(chemin));
	//this.image.setTaille(400, 320);
    }

    private static String resolveImagePath(String chemin) {
	String[] candidats = {
	    chemin + "/photo_small.png",
	    chemin + "/photo.png",
	    chemin + "/image.png",
	    "img/bouton2.png"
	};

	for (String candidat : candidats) {
	    if (new File(candidat).exists()) {
		return candidat;
	    }
	}

	return "img/bouton2.png";
    }

}
