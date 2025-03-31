/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.service;

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.ldap.*;
import javax.naming.directory.*;
import javax.naming.*;
import org.springframework.stereotype.Service;

/**
 *
 * @author S2026987
 */
@Service
public class LightweightDirectoryAccessProtocolService {

    private Hashtable<String, String> environment;
    private static final String ADMIN_NAME = "S101GC01";
    private static final String ADMIN_PASSWORD = "Password321";
    private static final String LDAP_URL = "ldap://sars.gov.za:389/dc=sars,dc=gov,dc=za";
    private static final String SEARCH_BASE = "OU=Users,OU=SARS";
    private static final String INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    private static final String SECURITY_AUTHENTICATION = "simple";

    public LightweightDirectoryAccessProtocolService() {
        environment = new Hashtable<String, String>();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY);
        environment.put(Context.SECURITY_AUTHENTICATION, SECURITY_AUTHENTICATION);
        environment.put(Context.SECURITY_PRINCIPAL, ADMIN_NAME.trim());
        environment.put(Context.SECURITY_CREDENTIALS, ADMIN_PASSWORD.trim());
        environment.put(Context.PROVIDER_URL, LDAP_URL.trim());
    }

    public String getUserEmailAddress(String sid) {
        try {
            DirContext ctx = new InitialLdapContext(environment, null);
            SearchControls searchCtls = new SearchControls();
            String returnedAtts[] = {"mail"};
            searchCtls.setReturningAttributes(returnedAtts);
            searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String searchFilter = "(samAccountName=" + sid + ")";
            NamingEnumeration answer = ctx.search(SEARCH_BASE, searchFilter, searchCtls);
            while (answer.hasMoreElements()) {
                SearchResult sr = (SearchResult) answer.next();
                if (sr.getAttributes() != null) {
                    return (String) sr.getAttributes().get("mail").get();
                }
            }
            ctx.close();
        } catch (Exception e) {
            Logger.getLogger(LightweightDirectoryAccessProtocolService.class.getName()).log(Level.SEVERE, null, e);
        }
        return "";
    }
}
