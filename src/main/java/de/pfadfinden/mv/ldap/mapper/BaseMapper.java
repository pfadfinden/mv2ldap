package de.pfadfinden.mv.ldap.mapper;

import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapInvalidAttributeValueException;

import java.util.ArrayList;
import java.util.List;

public class BaseMapper {
    protected String getLdapString(Entry entry, String attributeName) throws LdapInvalidAttributeValueException {
        if(isEmpty(attributeName)) return null;
        Attribute attribute = entry.get(attributeName);
        if(attribute != null){
            return attribute.getString();
        }
        return null;
    }

    protected List<String> getLdapList(Entry entry, String attributeName){
        if(isEmpty(attributeName)) return null;
        Attribute attribute = entry.get(attributeName);
        if(attribute != null) {
            List<String> values = new ArrayList<>();
            for(Value<?> value : attribute){
                values.add(value.toString());
            }
            return values;
        }
        return null;
    }

    protected boolean isEmpty(Object str){
        return (str == null || "".equals(str));
    }
}
