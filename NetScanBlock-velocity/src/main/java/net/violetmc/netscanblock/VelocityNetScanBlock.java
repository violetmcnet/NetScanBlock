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
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Plugin(
        id = "netscanblock",
        name = "NetScanBlock",
        version = "1.0",
        authors = {"MasterDash5"}
)
public final class VelocityNetScanBlock {

    private final Logger logger;
    private final List<String> allowed;

    @Inject
    public VelocityNetScanBlock(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) throws IOException {
        YamlDocument config = YamlDocument.create(
                new File(dataDirectory.toFile(), "config.yml"),
                Objects.requireNonNull(getClass().getResourceAsStream("/config.yml"))
        );
        config.save();

        this.logger = config.getBoolean("log-denied-connections") ? logger : null;
        this.allowed = config.getStringList("allowed-domains");
    }

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        InboundConnection connection = event.getConnection();

        if (isDomainDisallowed(connection)) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text("Connection Refused")));

            if (logger != null)
                logger.info("Denied connection for {} ({})", event.getUsername(), connection.getRemoteAddress().getHostName());
        }
    }

    @Subscribe
    public void onProxyPing(ProxyPingEvent event) {
        if (isDomainDisallowed(event.getConnection()))
            event.setResult(ResultedEvent.GenericResult.denied());
    }

    private boolean isDomainDisallowed(InboundConnection connection) {
        Optional<InetSocketAddress> address = connection.getVirtualHost();

        if (address.isEmpty())
            return true;

        String hostname = address.get().getHostName().toLowerCase();

        for (String domain : allowed)
            if (hostname.endsWith(domain))
                return false;

        return true;
    }

}
