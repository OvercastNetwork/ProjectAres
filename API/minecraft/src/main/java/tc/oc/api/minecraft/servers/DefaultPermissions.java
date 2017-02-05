package tc.oc.api.minecraft.servers;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * This is a hard-coded list of permissions, that add the basic functionality.
 * To be replaced when a proper rank/group & permission system
 *
 * The list of permissions have been copied from what they used to be on ocn, removing blatantly outdated ones.
 */
public class DefaultPermissions {

    public static final Map<String, Boolean> DEFAULT_PERMISSIONS;
    public static final Map<String, Boolean> PARTICIPANT_PERMISSIONS;
    public static final Map<String, Boolean> OBSERVER_PERMISSIONS;
    public static final Map<String, Boolean> MAPMAKER_PERMISSIONS;

    static {
        DEFAULT_PERMISSIONS = new ImmutableMap.Builder<String, Boolean>()
                // Global
                .put("ocn.login", true)
                // Tournament
                .put("tourney.ready", true)
                .build();

        PARTICIPANT_PERMISSIONS = new ImmutableMap.Builder<String, Boolean>()
                // Global
                .put("worldedit.navigation.jumpto.tool", false)
                .put("worldedit.navigation.thru.tool"  , false)
                // Untourney
                .put("bukkit.command.kill"     , false)
                .put("bukkit.command.me"       , false)
                .put("bukkit.command.tell"     , false)
                .put("commandbook.pong"        , false)
                .put("commandbook.speed.flight", false)
                .put("commandbook.speed.walk"  , false)
                .put("chat.global.receive"     , true)
                .put("commandbook.motd"        , true)
                .put("commandbook.msg"         , true)
                .put("commandbook.rules"       , true)
                .put("commandbook.time.check"  , true)
                .put("commandbook.who"         , true)
                .put("pgm.chat.report"         , true)
                .put("pgm.class"               , true)
                .put("pgm.class.list"          , true)
                .put("pgm.class.select"        , true)
                .put("pgm.join"                , true)
                .put("pgm.myteam"              , true)
                .build();

        OBSERVER_PERMISSIONS = new ImmutableMap.Builder<String, Boolean>()
                // Untourney
                .put("bukkit.command.kill"         , false)
                .put("bukkit.command.me"           , false)
                .put("bukkit.command.tell"         , false)
                .put("commandbook.pong"            , false)
                .put("worldedit.navigation.ceiling", false)
                .put("worldedit.navigation.up"     , false)
                .put("commandbook.motd"            , true)
                .put("commandbook.msg"             , true)
                .put("commandbook.rules"           , true)
                .put("commandbook.teleport"        , true)
                .put("commandbook.time.check"      , true)
                .put("commandbook.who"             , true)
                .put("ocn.teleport"                , true)
                .put("pgm.chat.report"             , true)
                .put("pgm.class"                   , true)
                .put("pgm.class.list"              , true)
                .put("pgm.class.select"            , true)
                .put("pgm.inventory"               , true)
                .put("pgm.join"                    , true)
                .put("pgm.myteam"                  , true)
                .put("worldedit.navigation.*"      , true)
                .build();

        MAPMAKER_PERMISSIONS = new ImmutableMap.Builder<String, Boolean>()
                // Public
                .put("map.rating.view.live", true)
                .put("pgm.fullserver"      , true)
                .put("pgm.join.choose"     , true)
                .put("pgm.join.full"       , true)
                .build();
    }

}
