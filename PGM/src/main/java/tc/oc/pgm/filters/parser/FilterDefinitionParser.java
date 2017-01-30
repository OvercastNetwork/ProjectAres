package tc.oc.pgm.filters.parser;

import java.time.Duration;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;

import com.google.common.collect.Range;
import org.bukkit.PoseFlag;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ImItemStack;
import org.jdom2.Element;
import tc.oc.api.docs.SemanticVersion;
import tc.oc.commons.bukkit.localization.MessageTemplate;
import tc.oc.commons.core.formatting.StringUtils;
import tc.oc.commons.core.util.Comparables;
import tc.oc.pgm.classes.ClassModule;
import tc.oc.pgm.classes.PlayerClass;
import tc.oc.pgm.features.FeatureDefinitionContext;
import tc.oc.pgm.features.FeatureDefinitionParser;
import tc.oc.pgm.features.FeatureParser;
import tc.oc.pgm.features.MagicMethodFeatureParser;
import tc.oc.pgm.filters.Filter;
import tc.oc.pgm.filters.matcher.CauseFilter;
import tc.oc.pgm.filters.matcher.StaticFilter;
import tc.oc.pgm.filters.matcher.block.MaterialFilter;
import tc.oc.pgm.filters.matcher.block.StructuralLoadFilter;
import tc.oc.pgm.filters.matcher.block.VoidFilter;
import tc.oc.pgm.filters.matcher.damage.AttackerFilter;
import tc.oc.pgm.filters.matcher.damage.DamagerFilter;
import tc.oc.pgm.filters.matcher.damage.RelationFilter;
import tc.oc.pgm.filters.matcher.damage.VictimFilter;
import tc.oc.pgm.filters.matcher.entity.EntityTypeFilter;
import tc.oc.pgm.filters.matcher.entity.SpawnReasonFilter;
import tc.oc.pgm.filters.matcher.match.FlagStateFilter;
import tc.oc.pgm.filters.matcher.match.LegacyRandomFilter;
import tc.oc.pgm.filters.matcher.match.MatchMutationFilter;
import tc.oc.pgm.filters.matcher.match.MatchStateFilter;
import tc.oc.pgm.filters.matcher.match.MonostableFilter;
import tc.oc.pgm.filters.matcher.match.PlayerCountFilter;
import tc.oc.pgm.filters.matcher.match.RandomFilter;
import tc.oc.pgm.filters.matcher.party.CompetitorFilter;
import tc.oc.pgm.filters.matcher.party.GoalFilter;
import tc.oc.pgm.filters.matcher.party.RankFilter;
import tc.oc.pgm.filters.matcher.party.ScoreFilter;
import tc.oc.pgm.filters.matcher.party.TeamFilter;
import tc.oc.pgm.filters.matcher.player.AttributeFilter;
import tc.oc.pgm.filters.matcher.player.CanFlyFilter;
import tc.oc.pgm.filters.matcher.player.CarryingFlagFilter;
import tc.oc.pgm.filters.matcher.player.CarryingItemFilter;
import tc.oc.pgm.filters.matcher.player.HoldingItemFilter;
import tc.oc.pgm.filters.matcher.player.KillStreakFilter;
import tc.oc.pgm.filters.matcher.player.ParticipatingFilter;
import tc.oc.pgm.filters.matcher.player.PlayerClassFilter;
import tc.oc.pgm.filters.matcher.player.PoseFilter;
import tc.oc.pgm.filters.matcher.player.WearingItemFilter;
import tc.oc.pgm.filters.operator.AllFilter;
import tc.oc.pgm.filters.operator.AnyFilter;
import tc.oc.pgm.filters.operator.FallthroughFilter;
import tc.oc.pgm.filters.operator.InverseFilter;
import tc.oc.pgm.filters.operator.OneFilter;
import tc.oc.pgm.filters.operator.SameTeamFilter;
import tc.oc.pgm.filters.operator.TeamFilterAdapter;
import tc.oc.pgm.flag.FlagDefinition;
import tc.oc.pgm.flag.Post;
import tc.oc.pgm.flag.state.Captured;
import tc.oc.pgm.flag.state.Carried;
import tc.oc.pgm.flag.state.Dropped;
import tc.oc.pgm.flag.state.Returned;
import tc.oc.pgm.flag.state.State;
import tc.oc.pgm.goals.GoalDefinition;
import tc.oc.pgm.itemmeta.ItemModifier;
import tc.oc.pgm.kits.ItemParser;
import tc.oc.pgm.map.MapProto;
import tc.oc.pgm.map.ProtoVersions;
import tc.oc.pgm.match.MatchState;
import tc.oc.pgm.match.PlayerRelation;
import tc.oc.pgm.mutation.Mutation;
import tc.oc.pgm.teams.TeamFactory;
import tc.oc.pgm.utils.MethodParser;
import tc.oc.pgm.utils.XMLUtils;
import tc.oc.pgm.xml.InvalidXMLException;
import tc.oc.pgm.xml.Node;
import tc.oc.pgm.xml.parser.Parser;
import tc.oc.pgm.xml.property.MessageTemplateProperty;
import tc.oc.pgm.xml.property.PropertyBuilderFactory;

import static tc.oc.commons.core.exception.LambdaExceptionUtils.rethrowFunction;

public class FilterDefinitionParser extends MagicMethodFeatureParser<Filter> implements FeatureDefinitionParser<Filter> {

    @Inject protected @MapProto SemanticVersion proto;
    @Inject protected FeatureDefinitionContext features;
    @Inject protected ItemParser itemParser;
    @Inject protected Parser<Attribute> attributeParser;
    @Inject protected PropertyBuilderFactory<MessageTemplate, MessageTemplateProperty> messageTemplates;
    @Inject protected CauseFilter.Factory causeFilters;
    @Inject protected FilterParser filterParser;
    @Inject protected FeatureParser<TeamFactory> teamParser;
    @Inject protected Provider<ClassModule> classModule;
    @Inject protected ItemModifier itemModifier;

    @MethodParser("allow")
    public Filter parseAllow(Element el) throws InvalidXMLException {
        return new FallthroughFilter(Filter.QueryResponse.ALLOW, filterParser.parseChild(el));
    }

    @MethodParser("deny")
    public Filter parseDeny(Element el) throws InvalidXMLException {
        return new FallthroughFilter(Filter.QueryResponse.DENY, filterParser.parseChild(el));
    }

    @MethodParser("always")
    public Filter parseAlways(Element el) {
        return new StaticFilter(Filter.QueryResponse.ALLOW);
    }

    @MethodParser("never")
    public Filter parseNever(Element el) {
        return new StaticFilter(Filter.QueryResponse.DENY);
    }

    @MethodParser("any")
    public Filter parseAny(Element el) throws InvalidXMLException {
        return new AnyFilter(filterParser.parseChildList(el));
    }

    @MethodParser("all")
    public Filter parseAll(Element el) throws InvalidXMLException {
        return new AllFilter(filterParser.parseChildList(el));
    }

    @MethodParser("one")
    public Filter parseOne(Element el) throws InvalidXMLException {
        return new OneFilter(filterParser.parseChildList(el));
    }

    @MethodParser("not")
    public Filter parseNot(Element el) throws InvalidXMLException {
        return new InverseFilter(filterParser.parseChild(el));
    }

    @MethodParser("team")
    public TeamFilter parseTeam(Element el) throws InvalidXMLException {
        return new TeamFilter(teamParser.parseReference(Node.of(el)));
    }

    @MethodParser("same-team")
    public SameTeamFilter parseSameTeam(Element el) throws InvalidXMLException {
        return new SameTeamFilter(filterParser.parseChild(el));
    }

    @MethodParser("attacker")
    public AttackerFilter parseAttacker(Element el) throws InvalidXMLException {
        return new AttackerFilter(filterParser.parseChild(el));
    }

    @MethodParser("victim")
    public VictimFilter parseVictim(Element el) throws InvalidXMLException {
        return new VictimFilter(filterParser.parseChild(el));
    }

    @MethodParser
    public Filter damager(Element el) throws InvalidXMLException {
        return new DamagerFilter(filterParser.parseChild(el));
    }

    @MethodParser("class")
    public PlayerClassFilter parseClass(Element el) throws InvalidXMLException {
        final PlayerClass playerClass = StringUtils.bestFuzzyMatch(el.getTextNormalize(), classModule.get().getPlayerClasses(), 0.9);
        if (playerClass == null) {
            throw new InvalidXMLException("Could not find player-class: " + el.getTextNormalize(), el);
        }

        return new PlayerClassFilter(playerClass);
    }

    @MethodParser("material")
    public MaterialFilter parseMaterial(Element el) throws InvalidXMLException {
        return new MaterialFilter(XMLUtils.parseMaterialPattern(el));
    }

    @MethodParser("void")
    public VoidFilter parseVoid(Element el) throws InvalidXMLException {
        return new VoidFilter();
    }

    @MethodParser("entity")
    public EntityTypeFilter parseEntity(Element el) throws InvalidXMLException {
        return new EntityTypeFilter(XMLUtils.parseEnum(el, EntityType.class, "entity type"));
    }

    @MethodParser("mob")
    public EntityTypeFilter parseMob(Element el) throws InvalidXMLException {
        EntityTypeFilter matcher = this.parseEntity(el);
        if(!LivingEntity.class.isAssignableFrom(matcher.getEntityType())) {
            throw new InvalidXMLException("Unknown mob type: " + el.getTextNormalize(), el);
        }
        return matcher;
    }

    @MethodParser("spawn")
    public SpawnReasonFilter parseSpawnReason(Element el) throws InvalidXMLException {
        return new SpawnReasonFilter(XMLUtils.parseEnum(new Node(el), CreatureSpawnEvent.SpawnReason.class, "spawn reason"));
    }

    @MethodParser("kill-streak")
    public KillStreakFilter parseKillStreak(Element el) throws InvalidXMLException {
        boolean repeat = XMLUtils.parseBoolean(el.getAttribute("repeat"), false);
        boolean persistent = XMLUtils.parseBoolean(el.getAttribute("persistent"), false);
        Integer count = XMLUtils.parseNumber(el.getAttribute("count"), Integer.class, (Integer) null);
        Integer min = XMLUtils.parseNumber(el.getAttribute("min"), Integer.class, (Integer) null);
        Integer max = XMLUtils.parseNumber(el.getAttribute("max"), Integer.class, (Integer) null);
        Range<Integer> range;

        if(count != null) {
            range = Range.singleton(count);
        } else if(min == null) {
            if(max == null) {
                throw new InvalidXMLException("kill-streak filter must have a count, min, or max", el);
            } else {
                range = Range.atMost(max);
            }
        } else {
            if(max == null) {
                range = Range.atLeast(min);
            } else {
                range = Range.closed(min, max);
            }
        }

        if(repeat && !range.hasUpperBound()) {
            throw new InvalidXMLException("repeating kill-streak filter must have a count or max", el);
        }

        return new KillStreakFilter(range, repeat, persistent);
    }

    @MethodParser("random")
    public Filter parseRandom(Element el) throws InvalidXMLException {
        Node node = new Node(el);
        Range<Double> chance;
        try {
            chance = Range.closedOpen(0d, XMLUtils.parseNumber(node, Double.class));
        } catch(InvalidXMLException e) {
            chance = XMLUtils.parseNumericRange(node, Double.class);
        }

        Range<Double> valid = Range.closed(0d, 1d);
        if (valid.encloses(chance)) {
            return proto.isNoOlderThan(ProtoVersions.EVENT_QUERIES) ? new RandomFilter(chance)
                                                                    : new LegacyRandomFilter(chance);
        } else {
            double lower = chance.hasLowerBound() ? chance.lowerEndpoint() : Double.NEGATIVE_INFINITY;
            double upper = chance.hasUpperBound() ? chance.upperEndpoint() : Double.POSITIVE_INFINITY;
            double invalid;
            if(!valid.contains(lower)) {
                invalid = lower;
            } else {
                invalid = upper;
            }

            throw new InvalidXMLException("chance value (" + invalid + ") is not between 0 and 1", el);
        }
    }

    @MethodParser("grounded")
    public Filter parseGrounded(Element el) throws InvalidXMLException {
        return new PoseFilter(PoseFlag.GROUNDED);
    }

    @MethodParser({"crouching", "sneaking"})
    public Filter parseCrouching(Element el) throws InvalidXMLException {
        return new PoseFilter(PoseFlag.SNEAKING);
    }

    @MethodParser("walking")
    public Filter parseWalking(Element el) throws InvalidXMLException {
        return new InverseFilter(new AnyFilter(new PoseFilter(PoseFlag.SNEAKING),
                                               new PoseFilter(PoseFlag.SPRINTING)));
    }

    @MethodParser("sprinting")
    public Filter parseSprinting(Element el) throws InvalidXMLException {
        return new PoseFilter(PoseFlag.SPRINTING);
    }

    @MethodParser("gliding")
    public Filter parseGlidingFilter(Element el) throws InvalidXMLException {
        return new PoseFilter(PoseFlag.GLIDING);
    }

    @MethodParser("flying")
    public Filter parseFlying(Element el) throws InvalidXMLException {
        return new PoseFilter(PoseFlag.FLYING);
    }

    @MethodParser("can-fly")
    public CanFlyFilter parseCanFly(Element el) throws InvalidXMLException {
        return new CanFlyFilter();
    }

    private Filter parseExplicitTeam(Element el, CompetitorFilter filter) throws InvalidXMLException {
        final boolean any = XMLUtils.parseBoolean(el.getAttribute("any"), false);
        final Optional<TeamFactory> team = teamParser.property(el).optional();
        if(any && team.isPresent()) {
            throw new InvalidXMLException("Cannot combine attributes 'team' and 'any'", el);
        }
        return any || team.isPresent() ? new TeamFilterAdapter(team, filter)
                                       : filter;
    }

    private GoalDefinition goalReference(Element el) throws InvalidXMLException {
        return features.reference(new Node(el), GoalDefinition.class);
    }

    private GoalFilter goalFilter(Element el) throws InvalidXMLException {
        return new GoalFilter(goalReference(el));
    }

    @MethodParser("objective")
    public Filter parseGoal(Element el) throws InvalidXMLException {
        return parseExplicitTeam(el, goalFilter(el));
    }

    @MethodParser("completed")
    public Filter parseCompleted(Element el) throws InvalidXMLException {
        return new TeamFilterAdapter(Optional.empty(), goalFilter(el));
    }

    @MethodParser("captured")
    public Filter parseCaptured(Element el) throws InvalidXMLException {
        final GoalFilter goal = goalFilter(el);
        final Optional<TeamFactory> team = teamParser.property(el).optional();
        return team.isPresent() ? new TeamFilterAdapter(team, goal)
                                : goal;
    }

    @MethodParser("rank")
    public Filter parseRankFilter(Element el) throws InvalidXMLException {
        return parseExplicitTeam(el, new RankFilter(XMLUtils.parseNumericRange(new Node(el), Integer.class)));
    }

    @MethodParser("score")
    public Filter parseScoreFilter(Element el) throws InvalidXMLException {
        return parseExplicitTeam(el, new ScoreFilter(XMLUtils.parseNumericRange(new Node(el), Integer.class)));
    }

    protected FlagStateFilter parseFlagState(Element el, Class<? extends State> state) throws InvalidXMLException {
        return new FlagStateFilter(features.reference(new Node(el), FlagDefinition.class),
                                   Node.tryAttr(el, "post").map(rethrowFunction(attr -> features.reference(attr, Post.class))),
                                   state);
    }

    @MethodParser("flag-carried")
    public FlagStateFilter parseFlagCarried(Element el) throws InvalidXMLException {
        return this.parseFlagState(el, Carried.class);
    }

    @MethodParser("flag-dropped")
    public FlagStateFilter parseFlagDropped(Element el) throws InvalidXMLException {
        return this.parseFlagState(el, Dropped.class);
    }

    @MethodParser("flag-returned")
    public FlagStateFilter parseFlagReturned(Element el) throws InvalidXMLException {
        return this.parseFlagState(el, Returned.class);
    }

    @MethodParser("flag-captured")
    public FlagStateFilter parseFlagCaptured(Element el) throws InvalidXMLException {
        return this.parseFlagState(el, Captured.class);
    }

    @MethodParser("carrying-flag")
    public CarryingFlagFilter parseCarryingFlag(Element el) throws InvalidXMLException {
        return new CarryingFlagFilter(features.reference(new Node(el), FlagDefinition.class));
    }

    @MethodParser("cause")
    public CauseFilter parseCause(Element el) throws InvalidXMLException {
        return causeFilters.create(XMLUtils.parseEnum(el, CauseFilter.Cause.class, "cause filter"));
    }

    @MethodParser("relation")
    public RelationFilter parseRelation(Element el) throws InvalidXMLException {
        return new RelationFilter(XMLUtils.parseEnum(el, PlayerRelation.class, "player relation filter"));
    }

    private ImItemStack parseFilterItem(Element el) throws InvalidXMLException {
        return itemModifier.modify(itemParser.parseRequiredItem(el)).immutableCopy();
    }

    @MethodParser("carrying")
    public CarryingItemFilter parseHasItem(Element el) throws InvalidXMLException {
        return new CarryingItemFilter(parseFilterItem(el));
    }

    @MethodParser("holding")
    public HoldingItemFilter parseHolding(Element el) throws InvalidXMLException {
        return new HoldingItemFilter(parseFilterItem(el));
    }

    @MethodParser("wearing")
    public WearingItemFilter parseWearingItem(Element el) throws InvalidXMLException {
        return new WearingItemFilter(parseFilterItem(el));
    }

    @MethodParser("structural-load")
    public StructuralLoadFilter parseStructuralLoad(Element el) throws InvalidXMLException {
        return new StructuralLoadFilter(XMLUtils.parseNumber(el, Integer.class));
    }

    @MethodParser("time")
    public Filter parseTimeFilter(Element el) throws InvalidXMLException {
        final Duration duration = XMLUtils.parseDuration(el, (Duration) null);
        if(Comparables.greaterThan(duration, Duration.ZERO)) {
            return new AllFilter(
                MatchStateFilter.started(),
                new MonostableFilter(
                    duration,
                    MatchStateFilter.running(),
                    Optional.empty()
                ).not()
            );
        } else {
            return new MatchStateFilter(MatchState.Running, MatchState.Finished);
        }
    }

    @MethodParser("countdown")
    public Filter parseCountdownFilter(Element el) throws InvalidXMLException {
        final Duration duration = XMLUtils.parseDuration(el, "duration").required();
        if(Comparables.greaterThan(duration, Duration.ZERO)) {
            return new MonostableFilter(duration,
                                        filterParser.parseReferenceOrChild(el),
                                        messageTemplates.property(el, "message")
                                                        .placeholders(Range.closed(0, 1))
                                                        .optional());
        } else {
            return new StaticFilter(Filter.QueryResponse.DENY);
        }
    }

    @MethodParser("mutation")
    public MatchMutationFilter parseMatchMutation(Element el) throws InvalidXMLException {
        return new MatchMutationFilter(XMLUtils.parseEnum(el, Mutation.class, "match mutation"));
    }

    @MethodParser("participating")
    public Filter parseParticipating(Element el) throws InvalidXMLException {
        return new ParticipatingFilter(true);
    }

    @MethodParser("observing")
    public Filter parseObserving(Element el) throws InvalidXMLException {
        return new ParticipatingFilter(false);
    }

    @MethodParser("match-started")
    public Filter parseMatchStarted(Element el) throws InvalidXMLException {
        return new MatchStateFilter(MatchState.Running, MatchState.Finished);
    }

    @MethodParser("match-running")
    public Filter parseMatchRunning(Element el) throws InvalidXMLException {
        return new MatchStateFilter(MatchState.Running);
    }

    @MethodParser("match-finished")
    public Filter parseMatchFinished(Element el) throws InvalidXMLException {
        return new MatchStateFilter(MatchState.Finished);
    }

    @MethodParser("players")
    public Filter parsePlayerCount(Element el) throws InvalidXMLException {
        return new PlayerCountFilter(filterParser.parseReferenceOrChild(el),
                                     XMLUtils.parseNumericRange(el, Integer.class, Range.atLeast(1)),
                                     XMLUtils.parseBoolean(el, "participants").optional(true),
                                     XMLUtils.parseBoolean(el, "observers").optional(false));
    }

    @MethodParser
    public Filter attribute(Element el) throws InvalidXMLException {
        final Node node = Node.of(el);
        return new AttributeFilter(attributeParser.parse(node),
                                   XMLUtils.parseNumericRange(el, Double.class));
    }
}
