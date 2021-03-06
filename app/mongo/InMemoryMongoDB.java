package mongo;

import akka.actor.CoordinatedShutdown;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.typesafe.config.Config;
import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.runtime.Network;


import javax.inject.Inject;
import java.io.IOException;

public final class InMemoryMongoDB  extends MongoDriver{
    private static MongodExecutable mongoEx;

    @Inject
    public InMemoryMongoDB(CoordinatedShutdown coordinatedShutdown, Config config) {
        super(config, coordinatedShutdown);

        this.init();
    }

    private void init() {
        if (mongoEx != null) {
            return;
        }
        try {
            IRuntimeConfig builder = new RuntimeConfigBuilder()
                    .defaults(Command.MongoD)
                    .processOutput(ProcessOutput.getDefaultInstanceSilent())
                    .build();
            MongodStarter starter = MongodStarter.getInstance(builder);
            mongoEx = starter.prepare(new MongodConfigBuilder()
                    .version(Version.Main.PRODUCTION)
                    .net(new Net("localhost", 12345, Network.localhostIsIPv6()))
                    .build());
            mongoEx.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MongoDatabase connect() {
        client = MongoClients.create("mongodb://localhost:27017");
        return client.getDatabase("test");
    }

    @Override
    public void disconnect() {
        closeMongoClient();
        closeMongoProcess();
    }

    private void closeMongoProcess() {
        if (mongoEx == null) {
            return;
        }
        mongoEx.stop();
    }

    private void closeMongoClient() {
        if (client == null) {
            return;
        }
        client.close();
    }

}
