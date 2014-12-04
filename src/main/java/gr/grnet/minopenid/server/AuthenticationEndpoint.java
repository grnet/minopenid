package gr.grnet.minopenid.server;
 
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

public class AuthenticationEndpoint extends HttpServlet {
    
    protected void doGet(HttpServletRequest request,
        HttpServletResponse response) throws ServletException, IOException {

        OAuthAuthzRequest oauthRequest = null;
        OAuthResponse resp = null;

        OAuthIssuerImpl oauthIssuerImpl =
            new OAuthIssuerImpl(new MD5Generator());

        try {
            try {
                oauthRequest = new OAuthAuthzRequest(request);
                
                String redirectURI =
                    oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI);
                
                resp = OAuthASResponse
                    .authorizationResponse(request,
                        HttpServletResponse.SC_FOUND)
                    .setCode(oauthIssuerImpl.authorizationCode())
                    .location(redirectURI)
                    .buildQueryMessage();
            } catch (OAuthProblemException pex) {
                String redirectUri = pex.getRedirectUri();
                resp = OAuthASResponse
                    .errorResponse(HttpServletResponse.SC_FOUND)
                    .error(pex)
                    .location(redirectUri)
                    .buildQueryMessage();
                
                response.sendRedirect(resp.getLocationUri());
            }
        } catch (OAuthSystemException sex) {
            System.out.println(sex.getMessage());
            System.out.println(sex.getStackTrace());
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().println("<h1>SERVER ERROR</h1>");
        }
        response.sendRedirect(resp.getLocationUri());
    }
}
