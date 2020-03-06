package bfst20.mapdrawer;

import javafx.scene.image.Image;

public class Model {

    public Model() {

    }

    //returnerer maps logoet
    public Image getIcon(){
        return new Image(this.getClass().getClassLoader().getResourceAsStream("mapslogo.png"));
    }
}
