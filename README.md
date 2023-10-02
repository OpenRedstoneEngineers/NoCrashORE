# NoCrashORE
#### Prevents redstone redirection tick loop crashes

### About
In Minecraft ≤ 1.18.2, the right arrangement of a trapdoor, powered rails,
and redstone dust can cause an infinite loop while processing a server tick,
crashing the server.

Of the three components involved, the trapdoors are used the least in redstone
and are also the most central to the cause of the loop, so they are the obvious
choice for what to monitor if you want to catch these loops with the smallest
overhead possible for other, benign redstone.

NoCrashORE will keep track of how many times a given trapdoor has updated in
the current tick, and if it exceeds the threshold (64 by default) then the
trapdoor will be beaned (replaced with air), and if configured, it will also
send a webhook notification with the coordinates where it happened.

### Config

| Option      | Description                                                         | Default |
|-------------|---------------------------------------------------------------------|---------|
| `threshold` | Max number of trapdoor updates per tick before getting beaned.      | `64`    |
| `webhook`   | If defined, the URL of the Discord webhook to notify after beaning. | `null`  |