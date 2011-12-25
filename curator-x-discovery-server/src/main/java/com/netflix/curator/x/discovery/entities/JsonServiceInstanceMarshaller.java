/*
 *
 *  Copyright 2011 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */

package com.netflix.curator.x.discovery.entities;

import com.netflix.curator.x.discovery.ServiceInstance;
import com.netflix.curator.x.discovery.ServiceInstanceBuilder;
import com.netflix.curator.x.discovery.ServiceType;
import com.netflix.curator.x.discovery.config.DiscoveryContext;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

public abstract class JsonServiceInstanceMarshaller<T> implements MessageBodyReader<ServiceInstance<T>>, MessageBodyWriter<ServiceInstance<T>>
{
    protected abstract DiscoveryContext<T>   getContext();

    static<T> ServiceInstance<T> readInstance(JsonNode node, DiscoveryContext<T> context) throws Exception
    {
        ServiceInstanceBuilder<T> builder = ServiceInstance.builder();

        builder.name(node.get("name").asText());
        builder.id(node.get("id").asText());
        builder.address(node.get("address").asText());
        builder.registrationTimeUTC(node.get("registrationTimeUTC").asLong());
        builder.serviceType(ServiceType.valueOf(node.get("serviceType").asText()));
        builder.payload(context.unMarshallJson(node.get("payload")));

        Integer port = getInteger(node, "port");
        Integer sslPort = getInteger(node, "sslPort");
        if ( port != null )
        {
            builder.port(port);
        }
        if ( sslPort != null )
        {
            builder.sslPort(sslPort);
        }

        return builder.build();
    }

    static<T> ObjectNode writeInstance(ObjectMapper mapper, ServiceInstance<T> instance, DiscoveryContext<T> context)
    {
        ObjectNode  node = mapper.createObjectNode();
        node.put("name", instance.getName());
        node.put("id", instance.getId());
        node.put("address", instance.getAddress());
        putInteger(node, "port", instance.getPort());
        putInteger(node, "sslPort", instance.getSslPort());
        node.put("registrationTimeUTC", instance.getRegistrationTimeUTC());
        node.put("serviceType", instance.getServiceType().name());
        try
        {
            context.marshallJson(node, "payload", instance);
        }
        catch ( Exception e )
        {
            throw new WebApplicationException(e);
        }

        return node;
    }

    private static Integer getInteger(JsonNode node, String fieldName)
    {
        JsonNode intNode = node.get(fieldName);
        return (intNode != null) ? intNode.asInt() : null;
    }

    private static void putInteger(ObjectNode node, String fieldName, Integer value)
    {
        if ( value != null )
        {
            node.put(fieldName, value);
        }
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return isWriteable(type, genericType, annotations, mediaType);
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return ServiceInstance.class.isAssignableFrom(type) && mediaType.equals(MediaType.APPLICATION_JSON_TYPE);
    }

    @Override
    public long getSize(ServiceInstance<T> serviceInstance, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
    {
        return -1;
    }

    @Override
    public ServiceInstance<T> readFrom(Class<ServiceInstance<T>> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
    {
        try
        {
            ObjectMapper                mapper = new ObjectMapper();
            JsonNode                    node = mapper.reader().readTree(entityStream);
            return readInstance(node, getContext());
        }
        catch ( Exception e )
        {
            throw new WebApplicationException(e);
        }
    }

    @Override
    public void writeTo(ServiceInstance<T> serviceInstance, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException
    {
        ObjectMapper    mapper = new ObjectMapper();
        ObjectNode      node = writeInstance(mapper, serviceInstance, getContext());
        mapper.writer().writeValue(entityStream, node);
    }
}
