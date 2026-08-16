package net.violetmc.netscanblock;
import com.google.inject.Inject;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.InboundConnection;
import com.velocitypowered.api.proxy.ProxyServer;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Plugin(
        id = "netscanblock",
        name = "NetScanBlock",
        version = "1.0",
        authors = {"MasterDash5"}
)
public class VelocityNetScanBlock {

    private final List<String> allowed;

    @Inject
    public VelocityNetScanBlock(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) throws IOException {
        YamlDocument config = YamlDocument.create(
                new File(dataDirectory.toFile(), "config.yml"),
                Objects.requireNonNull(getClass().getResourceAsStream("/config.yml"))
        );
        config.save();
        allowed = config.getStringList("allowed-domains");
    }

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        if (isDomainDisallowed(event.getConnection()))
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("Connection Refused")));
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        if (isDomainDisallowed(event.getConnection()))
            event.setResult(ResultedEvent.GenericResult.denied());
    }

    private boolean isDomainDisallowed(InboundConnection connection) {
        return connection.getVirtualHost().filter(
                address -> allowed.contains(address.getHostName().toLowerCase())
        ).isEmpty();
    }

}
