/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package org.apache.ofbiz.graphql.schema;

import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static graphql.schema.idl.TypeRuntimeWiring.newTypeWiring;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.ofbiz.base.util.FileUtil;
import org.apache.ofbiz.base.util.UtilXml;
import org.apache.ofbiz.entity.Delegator;
import org.apache.ofbiz.graphql.fetcher.EntityDataFetcher;
import org.apache.ofbiz.service.LocalDispatcher;
import org.w3c.dom.Element;

import GraphQLSchemaDefinition.FieldDefinition;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

public class GraphQLSchemaDefinition {
	
	private Delegator delegator;
	private LocalDispatcher dispatcher;
	
	public GraphQLSchemaDefinition(Delegator delegator, LocalDispatcher dispatcher, Map<String, Element> schemaMap) {
		this.delegator = delegator;
		schemaMap.forEach((k, v) -> {
			
		});
	}
	
	
	/**
	 * Creates a new GraphQLSchema using SDL
	 * @return
	 */
	public GraphQLSchema newSDLSchema() {
		SchemaParser schemaParser = new SchemaParser();
		SchemaGenerator schemaGenerator = new SchemaGenerator();
		Reader cdpSchemaReader = getSchemaReader("component://graphql/gql-schema/schema.graphqls");
		TypeDefinitionRegistry typeRegistry = new TypeDefinitionRegistry();
		typeRegistry.merge(schemaParser.parse(cdpSchemaReader));
		RuntimeWiring runtimeWiring = buildRuntimeWiring();
		GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);
		return graphQLSchema;
	}

	/**
	 * Creates a new GraphQLSchema dynamically
	 * @return
	 */
	public GraphQLSchema newDynamicSchema() {
		GraphQLObjectType productType = newObject().name("Product").field(newFieldDefinition().name("productId").type(GraphQLString))
				.field(newFieldDefinition().name("productId").type(GraphQLString))
				.field(newFieldDefinition().name("productName").type(GraphQLString))
				.field(newFieldDefinition().name("description").type(GraphQLString))
				.field(newFieldDefinition().name("productTypeId").type(GraphQLString))
				.field(newFieldDefinition().name("primaryProductCategoryId").type(GraphQLString))
		        .field(newFieldDefinition().name("isVirtual").type(GraphQLString)).build();
		GraphQLObjectType queryType = newObject().name("Query")
				.field(newFieldDefinition().name("product").type( productType).argument(GraphQLArgument.newArgument().name("id").type(GraphQLString))).build();
		GraphQLCodeRegistry codeRegistry = GraphQLCodeRegistry.newCodeRegistry()
				.dataFetcher(FieldCoordinates.coordinates("Query", "product"), new EntityDataFetcher())
				.build();
		GraphQLSchema schema = GraphQLSchema.newSchema().query(queryType).codeRegistry(codeRegistry).build();
		return schema;

	}

	private static Reader getSchemaReader(String resourceUrl) {
		File schemaFile = FileUtil.getFile(resourceUrl);
		try {
			return new InputStreamReader(new FileInputStream(schemaFile), StandardCharsets.UTF_8.name());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Builds Runtime Wiring for the schema types defined
	 * @return
	 */
	private RuntimeWiring buildRuntimeWiring() {
		RuntimeWiring.Builder build = RuntimeWiring.newRuntimeWiring();
		build.type(newTypeWiring("Query").dataFetcher("product", new EntityDataFetcher()));
		return build.build();
	}
	
	private static abstract class AbstractGraphQLTypeDefinition {
        String name;
        String description;
        String type;
        abstract List<String> getDependentTypes();
    }
	
	private static class ExtendObjectDefinition extends AbstractGraphQLTypeDefinition{
        final Delegator delegator;
        final LocalDispatcher dispatcher;
        List<Element> extendObjectNodeList = new ArrayList<Element>();
        String name, resolverField;
        List<String> excludeFields = new ArrayList<>();
        Map<String, String> resolverMap = new LinkedHashMap<>();
        
        ExtendObjectDefinition(Element node, Delegator delegator, LocalDispatcher dispatcher) {
            this.delegator = delegator;
            this.dispatcher = dispatcher;
            this.extendObjectNodeList.add(node);
            this.name = node.getAttribute("name");
            List<? extends Element> elements = UtilXml.childElementList(node);
            for (Element childNode : elements) {
            }
        }
	}   

}
