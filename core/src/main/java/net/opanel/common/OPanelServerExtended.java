package net.opanel.common;

import java.io.IOException;
import java.util.HashMap;

public interface OPanelServerExtended extends OPanelServer {
    HashMap<String, String> getCodeOfConducts() throws IOException;
    void updateOrCreateCodeOfConduct(String lang, String content) throws IOException;
    void removeCodeOfConduct(String lang) throws IOException;
}
