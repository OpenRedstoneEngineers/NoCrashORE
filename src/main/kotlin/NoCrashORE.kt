import com.destroystokyo.paper.event.server.ServerTickEndEvent
import org.bukkit.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.plugin.java.JavaPlugin
import java.net.HttpURLConnection
import java.net.URL


class NoCrashORE : JavaPlugin(), Listener {

    private val updateMap = HashMap<Location, Int>(1024)

    private val monitoredBlocks = hashSetOf(
        Material.IRON_TRAPDOOR,
        Material.OAK_TRAPDOOR,
        Material.BIRCH_TRAPDOOR,
        Material.SPRUCE_TRAPDOOR,
        Material.JUNGLE_TRAPDOOR,
        Material.ACACIA_TRAPDOOR,
        Material.DARK_OAK_TRAPDOOR,
        Material.WARPED_TRAPDOOR,
        Material.CRIMSON_TRAPDOOR,
    )

    private var threshold : Int     = 64
    private var webhook   : String? = null

    override fun onEnable() {
        server.pluginManager.registerEvents(this, this)

        saveDefaultConfig()

        config.run {
            threshold = getInt("threshold")
            webhook   = getString("webhook")
        }
    }

    @EventHandler
    private fun onBlockPhysics(event: BlockPhysicsEvent) {
        if (event.block.type in monitoredBlocks) {
            val count = 1 + (updateMap[event.block.location] ?: 0)
            updateMap[event.block.location] = count
            if (count > threshold) {
                event.isCancelled = true
                event.block.type = Material.AIR
                Bukkit.getScheduler().runTaskAsynchronously(this, Runnable { notify(event.block.location) })
            }
        }
    }

    @EventHandler
    private fun onTickEnd (event: ServerTickEndEvent) {
        updateMap.clear()
    }

    private fun notify(location: Location) {
        val where = "${location.blockX} ${location.blockY} ${location.blockZ}"
        logger.warning("Ope! Trapdoor at $where is changing an awful lot this tick. Beaned!")

        // *POOF!*
        location.world.spawnParticle(
            Particle.DUST_COLOR_TRANSITION,
            location.toCenterLocation(),
            25,
            0.5, 0.5, 0.5,
            Particle.DustTransition(
                Color.BLACK,
                Color.WHITE,
                1.0f
            )
        )

        // CRACK!
        location.world.playSound(
            location,
            Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR,
            SoundCategory.BLOCKS,
            1.0f,
            1.0f
        )

        if (webhook != null) {
            try {
                (URL(webhook).openConnection() as HttpURLConnection).run {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("User-Agent", "NoCrashORE")
                    doOutput = true
                    outputStream.use { stream ->
                        val input: ByteArray = (
                                "{\"embeds\":[{" +
                                        "\"title\":\"Prevented a crash maybe!\"," +
                                        "\"description\":\"Beaned a trapdoor at `$where`!\"," +
                                        "\"color\":16711680," +
                                        "\"author\":{" +
                                        "\"name\":\"NoCrashORE\"" +
                                        "}," +
                                        "\"footer\":{" +
                                        "\"text\":\"🫘\"" +
                                        "}" +
                                        "}]}"
                                ).encodeToByteArray()
                        stream.write(input, 0, input.size)
                        stream.flush()
                    }
                    inputStream.close()
                }
            } catch (exception: Exception) {
                logger.warning("Webhook request failed!: $exception")
            }
        }
    }
}
