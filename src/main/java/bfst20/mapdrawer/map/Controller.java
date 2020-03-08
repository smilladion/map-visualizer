package bfst20.mapdrawer.map;

import bfst20.mapdrawer.osm.OSMMap;

public class Controller {

    View view;
    OSMMap model;

    public Controller(OSMMap model) {
        this.model = model;
    }

    public void updateLastSearchedText() {
        view.getLabel().setText(view.getSearchBar().getText());
    }
}


