/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.gov.sars.amakhwezi.service.mail;

import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

/**
 *
 * @author S2026064
 */
public class LDAPService {

    private Hashtable<String, String> environment;
    private static final String ADMIN_NAME = "S101GC01";
    private static final String ADMIN_PASSWORD = "Password321";
    private static final String LDAP_URL = "ldap://sars.gov.za:389/dc=sars,dc=gov,dc=za";
    private static final String SEARCH_BASE = "OU=Users,OU=SARS";
    private static final String INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
    private static final String SECURITY_AUTHENTICATION = "simple";

    public LDAPService() {
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
            Logger.getLogger(LDAPService.class.getName()).log(Level.SEVERE, null, e);
        }
        return "";
    }
}
