package com.noteapp.config;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.ValidationLevel;
import com.mongodb.client.model.ValidationOptions;
import org.bson.Document;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.schema.MongoJsonSchema;
import static org.springframework.data.mongodb.core.schema.JsonSchemaProperty.string;

@Configuration
public class MongoSchemaConfig {

    private final MongoTemplate mongoTemplate;

    public MongoSchemaConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void initSchema() {

        MongoJsonSchema schema = MongoJsonSchema.builder()
                .required("title", "text")
                .properties(
                        string("title").minLength(1),
                        string("text").minLength(1)
                )
                .build();

        String collectionName = "notes";
        MongoDatabase db = mongoTemplate.getDb();

        Document jsonSchemaDoc = schema.toDocument();

        if (!mongoTemplate.collectionExists(collectionName)) {
            db.createCollection(
                    collectionName,
                    new CreateCollectionOptions()
                            .validationOptions(
                                    new ValidationOptions()
                                            .validator(jsonSchemaDoc)
                                            .validationLevel(ValidationLevel.STRICT)
                            )
            );
        }
    }
}

