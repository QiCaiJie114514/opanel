package net.opanel.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.opanel.OPanel;
import net.opanel.utils.Utils;
import net.opanel.web.BaseServlet;
import net.opanel.web.JwtManager;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;

public class AuthServlet extends BaseServlet {
    public static final String route = "/api/auth";
    private final HashMap<String, String> cramMap = new HashMap<>();

    public AuthServlet(OPanel plugin) {
        super(plugin);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        final String id = req.getParameter("id");
        if(id == null) {
            sendResponse(res, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String cramRandomHex = Utils.generateRandomHex(16);
        while(cramMap.containsValue(cramRandomHex)) {
            cramRandomHex = Utils.generateRandomHex(16);
        }
        cramMap.put(id, cramRandomHex);

        HashMap<String, Object> obj = new HashMap<>();
        obj.put("cram", cramRandomHex);
        sendResponse(res, obj);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        RequestBodyType reqBody = getRequestBody(req, RequestBodyType.class);
        if(reqBody.id == null || reqBody.result == null) {
            sendResponse(res, HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        final String challengeResult = reqBody.result; // hashed 3
        final String storedRealKey = plugin.getConfig().accessKey; // hashed 2
        final String realResult = Utils.md5(storedRealKey + cramMap.get(reqBody.id)); // hashed 3
        cramMap.remove(reqBody.id);

        if(challengeResult.equals(realResult)) {
            HashMap<String, Object> obj = new HashMap<>();
            obj.put("token", JwtManager.generateToken(storedRealKey, plugin.getConfig().salt));
            sendResponse(res, obj);
        } else {
            final String remoteHost = req.getRemoteHost();
            final int remotePort = req.getRemotePort();
            plugin.logger.warn("A failed login request from "+ remoteHost +":"+ remotePort);
            sendResponse(res, HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private class RequestBodyType {
        String id;
        String result; // Challenge result
    }
}
