/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.gov.sars.amakhwezi.mb.util;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author S2026987
 */
@FacesConverter(value = "entityConverter")
public class EntityConverter implements Converter{
    private static final Map<Object, String> entities = new WeakHashMap<Object, String>();

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object entity) {
        synchronized (entities) {
            if (!entities.containsKey(entity)) {
                String uuid = UUID.randomUUID().toString();
                entities.put(entity, uuid);
                return uuid;
            } else {
                return entities.get(entity);
            }
        }
    }
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String uuid) {
        for (Map.Entry<Object, String> entry : entities.entrySet()) {
            if (entry.getValue().equals(uuid)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
