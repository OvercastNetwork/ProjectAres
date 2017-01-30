package tc.oc.pgm.tracker.damage;

public interface MeleeInfo extends PhysicalInfo, DamageInfo {
    ItemInfo getWeapon();
}
