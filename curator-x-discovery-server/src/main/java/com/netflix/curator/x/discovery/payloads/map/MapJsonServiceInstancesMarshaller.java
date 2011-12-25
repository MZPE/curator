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

package com.netflix.curator.x.discovery.payloads.map;

import com.netflix.curator.x.discovery.config.DiscoveryContext;
import com.netflix.curator.x.discovery.entities.JsonServiceInstancesMarshaller;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class MapJsonServiceInstancesMarshaller extends JsonServiceInstancesMarshaller<Map<String, String>>
{
    @Context private ContextResolver<DiscoveryContext<Map<String, String>>> resolver;

    @Override
    protected DiscoveryContext<Map<String, String>> getContext()
    {
        return resolver.getContext(DiscoveryContext.class);
    }
}
