/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.struct;

import r48.io.data.DMContext;
import r48.io.data.obj.DMFXOBinding;
import r48.io.data.obj.DMOptional;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;

public class Terms extends DM2R2kObject {
  @DMFXOBinding("annXEncounter") @DM2LcfBinding(0x1) @DMCXObject
  public StringR2kStruct annXEncounter;

  @DMFXOBinding("preemptiveAttack") @DM2LcfBinding(0x2) @DMCXObject
  public StringR2kStruct preemptiveAttack;

  @DMFXOBinding("ranAwaySafely") @DM2LcfBinding(0x3) @DMCXObject
  public StringR2kStruct ranAwaySafely;

  @DMFXOBinding("failedToEscape") @DM2LcfBinding(0x4) @DMCXObject
  public StringR2kStruct failedToEscape;

  @DMFXOBinding("wonACombat") @DM2LcfBinding(0x5) @DMCXObject
  public StringR2kStruct wonACombat;

  @DMFXOBinding("partyDead") @DM2LcfBinding(0x6) @DMCXObject
  public StringR2kStruct partyDead;

  @DMFXOBinding("experience") @DM2LcfBinding(0x7) @DMCXObject
  public StringR2kStruct experience;

  @DMFXOBinding("youFound") @DM2LcfBinding(0x8) @DMCXObject
  public StringR2kStruct youFound;

  @DMFXOBinding("xMoney") @DM2LcfBinding(0x9) @DMCXObject
  public StringR2kStruct xMoney;

  @DMFXOBinding("xReceived") @DM2LcfBinding(0xA) @DMCXObject
  public StringR2kStruct xReceived;

  @DMFXOBinding("annXAttacksTheEnemy2KO") @DM2LcfBinding(0xB) @DMCXObject
  public StringR2kStruct annXAttacksTheEnemy2KO;

  @DMFXOBinding("annCritEnemyHurt2KO") @DM2LcfBinding(0xC) @DMCXObject
  public StringR2kStruct annCritEnemyHurt2KO;

  @DMFXOBinding("annCritPlayersHurt2KO") @DM2LcfBinding(0xD) @DMCXObject
  public StringR2kStruct annCritPlayersHurt2KO;

  @DMFXOBinding("annXGuarding2KO") @DM2LcfBinding(0xE) @DMCXObject
  public StringR2kStruct annXGuarding2KO;

  @DMFXOBinding("annXDoesNothing2KO") @DM2LcfBinding(0xF) @DMCXObject
  public StringR2kStruct annXDoesNothing2KO;

  @DMFXOBinding("annXReadies2Attacks2KO") @DM2LcfBinding(0x10) @DMCXObject
  public StringR2kStruct annXReadies2Attacks2KO;

  @DMFXOBinding("annXSelfDestructs2KO") @DM2LcfBinding(0x11) @DMCXObject
  public StringR2kStruct annXSelfDestructs2KO;

  @DMFXOBinding("annXRunsAway2KO") @DM2LcfBinding(0x12) @DMCXObject
  public StringR2kStruct annXRunsAway2KO;

  @DMFXOBinding("annXTransforms2KO") @DM2LcfBinding(0x13) @DMCXObject
  public StringR2kStruct annXTransforms2KO;

  @DMFXOBinding("annEHurtForXHP2KO") @DM2LcfBinding(0x14) @DMCXObject
  public StringR2kStruct annEHurtForXHP2KO;

  @DMFXOBinding("annXMissedE2KO") @DM2LcfBinding(0x15) @DMCXObject
  public StringR2kStruct annXMissedE2KO;

  @DMFXOBinding("annPHurtForXHP2KO") @DM2LcfBinding(0x16) @DMCXObject
  public StringR2kStruct annPHurtForXHP2KO;

  @DMFXOBinding("annXMissedP2KO") @DM2LcfBinding(0x17) @DMCXObject
  public StringR2kStruct annXMissedP2KO;

  @DMFXOBinding("annXFloppedSkillA2KO") @DM2LcfBinding(0x18) @DMCXObject
  public StringR2kStruct annXFloppedSkillA2KO;

  @DMFXOBinding("annXFloppedSkillB2KO") @DM2LcfBinding(0x19) @DMCXObject
  public StringR2kStruct annXFloppedSkillB2KO;

  @DMFXOBinding("annXFloppedSkillC2KO") @DM2LcfBinding(0x1A) @DMCXObject
  public StringR2kStruct annXFloppedSkillC2KO;

  @DMFXOBinding("annXDodged2KO") @DM2LcfBinding(0x1B) @DMCXObject
  public StringR2kStruct annXDodged2KO;

  @DMFXOBinding("annUsesItem2KO") @DM2LcfBinding(0x1C) @DMCXObject
  public StringR2kStruct annUsesItem2KO;

  @DMFXOBinding("annXRecovered2KO") @DM2LcfBinding(0x1D) @DMCXObject
  public StringR2kStruct annXRecovered2KO;

  @DMFXOBinding("paramInc2KO") @DM2LcfBinding(0x1E) @DMCXObject
  public StringR2kStruct paramInc2KO;

  @DMFXOBinding("paramDec2KO") @DM2LcfBinding(0x1F) @DMCXObject
  public StringR2kStruct paramDec2KO;

  @DMFXOBinding("annXHPAbsorbedFromE2KO") @DM2LcfBinding(0x20) @DMCXObject
  public StringR2kStruct annXHPAbsorbedFromE2KO;

  @DMFXOBinding("annXHPAbsorbedFromP2KO") @DM2LcfBinding(0x21) @DMCXObject
  public StringR2kStruct annXHPAbsorbedFromP2KO;

  @DMFXOBinding("resistInc2KO") @DM2LcfBinding(0x22) @DMCXObject
  public StringR2kStruct resistInc2KO;

  @DMFXOBinding("resistDec2KO") @DM2LcfBinding(0x23) @DMCXObject
  public StringR2kStruct resistDec2KO;

  @DMFXOBinding("levelUp") @DM2LcfBinding(0x24) @DMCXObject
  public StringR2kStruct levelUp;

  @DMFXOBinding("annLearnedX") @DM2LcfBinding(0x25) @DMCXObject
  public StringR2kStruct annLearnedX;

  @DMFXOBinding("battleStart") @DM2LcfBinding(0x26) @DMCXObject
  public StringR2kStruct battleStart;

  @DMFXOBinding("floatTxtMiss2k3") @DM2LcfBinding(0x27) @DMCXObject
  public StringR2kStruct floatTxtMiss2k3;

  @DMFXOBinding("shop1Greet") @DM2LcfBinding(0x29) @DMCXObject
  public StringR2kStruct shop1Greet;

  @DMFXOBinding("shop1Impatient") @DM2LcfBinding(0x2A) @DMCXObject
  public StringR2kStruct shop1Impatient;

  @DMFXOBinding("shop1Buy") @DM2LcfBinding(0x2B) @DMCXObject
  public StringR2kStruct shop1Buy;

  @DMFXOBinding("shop1Sell") @DM2LcfBinding(0x2C) @DMCXObject
  public StringR2kStruct shop1Sell;

  @DMFXOBinding("shop1Leave") @DM2LcfBinding(0x2D) @DMCXObject
  public StringR2kStruct shop1Leave;

  @DMFXOBinding("shop1WTBQuestion") @DM2LcfBinding(0x2E) @DMCXObject
  public StringR2kStruct shop1WTBQuestion;

  @DMFXOBinding("shop1ICoBQuestion") @DM2LcfBinding(0x2F) @DMCXObject
  public StringR2kStruct shop1ICoBQuestion;

  @DMFXOBinding("shop1Done") @DM2LcfBinding(0x30) @DMCXObject
  public StringR2kStruct shop1Done;

  @DMFXOBinding("shop1WTSQuestion") @DM2LcfBinding(0x31) @DMCXObject
  public StringR2kStruct shop1WTSQuestion;

  @DMFXOBinding("shop1ICoSQuestion") @DM2LcfBinding(0x32) @DMCXObject
  public StringR2kStruct shop1ICoSQuestion;

  @DMFXOBinding("shop1Thanks") @DM2LcfBinding(0x33) @DMCXObject
  public StringR2kStruct shop1Thanks;

  @DMFXOBinding("shop2Greet") @DM2LcfBinding(0x36) @DMCXObject
  public StringR2kStruct shop2Greet;

  @DMFXOBinding("shop2Impatient") @DM2LcfBinding(0x37) @DMCXObject
  public StringR2kStruct shop2Impatient;

  @DMFXOBinding("shop2Buy") @DM2LcfBinding(0x38) @DMCXObject
  public StringR2kStruct shop2Buy;

  @DMFXOBinding("shop2Sell") @DM2LcfBinding(0x39) @DMCXObject
  public StringR2kStruct shop2Sell;

  @DMFXOBinding("shop2Leave") @DM2LcfBinding(0x3A) @DMCXObject
  public StringR2kStruct shop2Leave;

  @DMFXOBinding("shop2WTBQuestion") @DM2LcfBinding(0x3B) @DMCXObject
  public StringR2kStruct shop2WTBQuestion;

  @DMFXOBinding("shop2ICoBQuestion") @DM2LcfBinding(0x3C) @DMCXObject
  public StringR2kStruct shop2ICoBQuestion;

  @DMFXOBinding("shop2Done") @DM2LcfBinding(0x3D) @DMCXObject
  public StringR2kStruct shop2Done;

  @DMFXOBinding("shop2WTSQuestion") @DM2LcfBinding(0x3E) @DMCXObject
  public StringR2kStruct shop2WTSQuestion;

  @DMFXOBinding("shop2ICoSQuestion") @DM2LcfBinding(0x3F) @DMCXObject
  public StringR2kStruct shop2ICoSQuestion;

  @DMFXOBinding("shop2Thanks") @DM2LcfBinding(0x40) @DMCXObject
  public StringR2kStruct shop2Thanks;

  @DMFXOBinding("shop3Greet") @DM2LcfBinding(0x43) @DMCXObject
  public StringR2kStruct shop3Greet;

  @DMFXOBinding("shop3Impatient") @DM2LcfBinding(0x44) @DMCXObject
  public StringR2kStruct shop3Impatient;

  @DMFXOBinding("shop3Buy") @DM2LcfBinding(0x45) @DMCXObject
  public StringR2kStruct shop3Buy;

  @DMFXOBinding("shop3Sell") @DM2LcfBinding(0x46) @DMCXObject
  public StringR2kStruct shop3Sell;

  @DMFXOBinding("shop3Leave") @DM2LcfBinding(0x47) @DMCXObject
  public StringR2kStruct shop3Leave;

  @DMFXOBinding("shop3WTBQuestion") @DM2LcfBinding(0x48) @DMCXObject
  public StringR2kStruct shop3WTBQuestion;

  @DMFXOBinding("shop3ICoBQuestion") @DM2LcfBinding(0x49) @DMCXObject
  public StringR2kStruct shop3ICoBQuestion;

  @DMFXOBinding("shop3Done") @DM2LcfBinding(0x4A) @DMCXObject
  public StringR2kStruct shop3Done;

  @DMFXOBinding("shop3WTSQuestion") @DM2LcfBinding(0x4B) @DMCXObject
  public StringR2kStruct shop3WTSQuestion;

  @DMFXOBinding("shop3ICoSQuestion") @DM2LcfBinding(0x4C) @DMCXObject
  public StringR2kStruct shop3ICoSQuestion;

  @DMFXOBinding("shop3Thanks") @DM2LcfBinding(0x4D) @DMCXObject
  public StringR2kStruct shop3Thanks;

  @DMFXOBinding("inn1PriceX") @DM2LcfBinding(0x50) @DMCXObject
  public StringR2kStruct inn1PriceX;

  @DMFXOBinding("inn1XGoldToSleep") @DM2LcfBinding(0x51) @DMCXObject
  public StringR2kStruct inn1XGoldToSleep;

  @DMFXOBinding("inn1ConfirmSleep") @DM2LcfBinding(0x52) @DMCXObject
  public StringR2kStruct inn1ConfirmSleep;

  @DMFXOBinding("inn1SleepYes") @DM2LcfBinding(0x53) @DMCXObject
  public StringR2kStruct inn1SleepYes;

  @DMFXOBinding("inn1SleepNo") @DM2LcfBinding(0x54) @DMCXObject
  public StringR2kStruct inn1SleepNo;

  @DMFXOBinding("inn2PriceX") @DM2LcfBinding(0x55) @DMCXObject
  public StringR2kStruct inn2PriceX;

  @DMFXOBinding("inn2XGoldToSleep") @DM2LcfBinding(0x56) @DMCXObject
  public StringR2kStruct inn2XGoldToSleep;

  @DMFXOBinding("inn2ConfirmSleep") @DM2LcfBinding(0x57) @DMCXObject
  public StringR2kStruct inn2ConfirmSleep;

  @DMFXOBinding("inn2SleepYes") @DM2LcfBinding(0x58) @DMCXObject
  public StringR2kStruct inn2SleepYes;

  @DMFXOBinding("inn2SleepNo") @DM2LcfBinding(0x59) @DMCXObject
  public StringR2kStruct inn2SleepNo;

  @DMFXOBinding("headOwnedItems") @DM2LcfBinding(0x5C) @DMCXObject
  public StringR2kStruct headOwnedItems;

  @DMFXOBinding("headEquippedItems") @DM2LcfBinding(0x5D) @DMCXObject
  public StringR2kStruct headEquippedItems;

  @DMFXOBinding("goldPostfix") @DM2LcfBinding(0x5F) @DMCXObject
  public StringR2kStruct goldPostfix;

  @DMFXOBinding("cmdFight") @DM2LcfBinding(0x65) @DMCXObject
  public StringR2kStruct cmdFight;

  @DMFXOBinding("cmdAuto") @DM2LcfBinding(0x66) @DMCXObject
  public StringR2kStruct cmdAuto;

  @DMFXOBinding("cmdEscape") @DM2LcfBinding(0x67) @DMCXObject
  public StringR2kStruct cmdEscape;

  @DMFXOBinding("cmdAttack2KO") @DM2LcfBinding(0x68) @DMCXObject
  public StringR2kStruct cmdAttack2KO;

  @DMFXOBinding("cmdDefend2KO") @DM2LcfBinding(0x69) @DMCXObject
  public StringR2kStruct cmdDefend2KO;

  @DMFXOBinding("cmdItemX") @DM2LcfBinding(0x6A) @DMCXObject
  public StringR2kStruct cmdItemX;

  @DMFXOBinding("cmdSkill") @DM2LcfBinding(0x6B) @DMCXObject
  public StringR2kStruct cmdSkill;

  @DMFXOBinding("optEquipment") @DM2LcfBinding(0x6C) @DMCXObject
  public StringR2kStruct optEquipment;

  @DMFXOBinding("optSave") @DM2LcfBinding(0x6E) @DMCXObject
  public StringR2kStruct optSave;

  @DMFXOBinding("optStopGame") @DM2LcfBinding(0x70) @DMCXObject
  public StringR2kStruct optStopGame;

  @DMFXOBinding("optStart") @DM2LcfBinding(0x72) @DMCXObject
  public StringR2kStruct optStart;

  @DMFXOBinding("optLoad") @DM2LcfBinding(0x73) @DMCXObject
  public StringR2kStruct optLoad;

  @DMFXOBinding("optExitApp") @DM2LcfBinding(0x75) @DMCXObject
  public StringR2kStruct optExitApp;

  @DMFXOBinding("menuStatus") @DM2LcfBinding(0x76) @DMCXObject
  public StringR2kStruct menuStatus;

  @DMFXOBinding("menuRow") @DM2LcfBinding(0x77) @DMCXObject
  public StringR2kStruct menuRow;

  @DMFXOBinding("menuOrder") @DM2LcfBinding(0x78) @DMCXObject
  public StringR2kStruct menuOrder;

  @DMFXOBinding("menuWaitOn") @DM2LcfBinding(0x79) @DMCXObject
  public StringR2kStruct menuWaitOn;

  @DMFXOBinding("menuWaitOff") @DM2LcfBinding(0x7A) @DMCXObject
  public StringR2kStruct menuWaitOff;

  @DMFXOBinding("batStatLevel") @DM2LcfBinding(0x7B) @DMCXObject
  public StringR2kStruct batStatLevel;

  @DMFXOBinding("batStatHP") @DM2LcfBinding(0x7C) @DMCXObject
  public StringR2kStruct batStatHP;

  @DMFXOBinding("batStatSP") @DM2LcfBinding(0x7D) @DMCXObject
  public StringR2kStruct batStatSP;

  @DMFXOBinding("batStatNoStateFx") @DM2LcfBinding(0x7E) @DMCXObject
  public StringR2kStruct batStatNoStateFx;

  @DMFXOBinding("exp2ch") @DM2LcfBinding(0x7F) @DMCXObject
  public StringR2kStruct exp2ch;

  @DMFXOBinding("lvl2ch") @DM2LcfBinding(0x80) @DMCXObject
  public StringR2kStruct lvl2ch;

  @DMFXOBinding("hp2ch") @DM2LcfBinding(0x81) @DMCXObject
  public StringR2kStruct hp2ch;

  @DMFXOBinding("sp2ch") @DM2LcfBinding(0x82) @DMCXObject
  public StringR2kStruct sp2ch;

  @DMFXOBinding("spCost") @DM2LcfBinding(0x83) @DMCXObject
  public StringR2kStruct spCost;

  @DMFXOBinding("statAtk") @DM2LcfBinding(0x84) @DMCXObject
  public StringR2kStruct statAtk;

  @DMFXOBinding("statDef") @DM2LcfBinding(0x85) @DMCXObject
  public StringR2kStruct statDef;

  @DMFXOBinding("statSpi") @DM2LcfBinding(0x86) @DMCXObject
  public StringR2kStruct statSpi;

  @DMFXOBinding("statAgi") @DM2LcfBinding(0x87) @DMCXObject
  public StringR2kStruct statAgi;

  @DMFXOBinding("equipWeapon") @DM2LcfBinding(0x88) @DMCXObject
  public StringR2kStruct equipWeapon;

  @DMFXOBinding("equipShield") @DM2LcfBinding(0x89) @DMCXObject
  public StringR2kStruct equipShield;

  @DMFXOBinding("equipArmour") @DM2LcfBinding(0x8A) @DMCXObject
  public StringR2kStruct equipArmour;

  @DMFXOBinding("equipHelmet") @DM2LcfBinding(0x8B) @DMCXObject
  public StringR2kStruct equipHelmet;

  @DMFXOBinding("equipAccessory") @DM2LcfBinding(0x8C) @DMCXObject
  public StringR2kStruct equipAccessory;

  @DMFXOBinding("saveSlot") @DM2LcfBinding(0x92) @DMCXObject
  public StringR2kStruct saveSlot;

  @DMFXOBinding("loadSlot") @DM2LcfBinding(0x93) @DMCXObject
  public StringR2kStruct loadSlot;

  @DMFXOBinding("file") @DM2LcfBinding(0x94) @DMCXObject
  public StringR2kStruct file;

  @DMFXOBinding("exitGame") @DM2LcfBinding(0x97) @DMCXObject
  public StringR2kStruct exitGame;

  @DMFXOBinding("yes") @DM2LcfBinding(0x98) @DMCXObject
  public StringR2kStruct yes;

  @DMFXOBinding("no") @DM2LcfBinding(0x99) @DMCXObject
  public StringR2kStruct no;

  @DMOptional @DMFXOBinding("easyrpgItemNumberSeparator") @DM2LcfBinding(0xC8) @DMCXObject
  public StringR2kStruct easyrpgItemNumberSeparator;

  @DMOptional @DMFXOBinding("easyrpgSkillCostSeparator") @DM2LcfBinding(0xC9) @DMCXObject
  public StringR2kStruct easyrpgSkillCostSeparator;

  @DMOptional @DMFXOBinding("easyrpgEquipmentArrow") @DM2LcfBinding(0xCA) @DMCXObject
  public StringR2kStruct easyrpgEquipmentArrow;

  @DMOptional @DMFXOBinding("easyrpgStatusSceneName") @DM2LcfBinding(0xCB) @DMCXObject
  public StringR2kStruct easyrpgStatusSceneName;

  @DMOptional @DMFXOBinding("easyrpgStatusSceneClass") @DM2LcfBinding(0xCC) @DMCXObject
  public StringR2kStruct easyrpgStatusSceneClass;

  @DMOptional @DMFXOBinding("easyrpgStatusSceneTitle") @DM2LcfBinding(0xCD) @DMCXObject
  public StringR2kStruct easyrpgStatusSceneTitle;

  @DMOptional @DMFXOBinding("easyrpgStatusSceneCondition") @DM2LcfBinding(0xCE) @DMCXObject
  public StringR2kStruct easyrpgStatusSceneCondition;

  @DMOptional @DMFXOBinding("easyrpgStatusSceneFront") @DM2LcfBinding(0xCF) @DMCXObject
  public StringR2kStruct easyrpgStatusSceneFront;

  @DMOptional @DMFXOBinding("easyrpgStatusSceneBack") @DM2LcfBinding(0xD0) @DMCXObject
  public StringR2kStruct easyrpgStatusSceneBack;

  @DMOptional @DMFXOBinding("easyrpgOrderSceneConfirm") @DM2LcfBinding(0xD1) @DMCXObject
  public StringR2kStruct easyrpgOrderSceneConfirm;

  @DMOptional @DMFXOBinding("easyrpgOrderSceneRedo") @DM2LcfBinding(0xD2) @DMCXObject
  public StringR2kStruct easyrpgOrderSceneRedo;

  @DMOptional @DMFXOBinding("easyrpgAnnBattleDoubleAttack2k3") @DM2LcfBinding(0xD3) @DMCXObject
  public StringR2kStruct easyrpgAnnBattleDoubleAttack2k3;

  @DMOptional @DMFXOBinding("easyrpgAnnBattleDefend2k3") @DM2LcfBinding(0xD4) @DMCXObject
  public StringR2kStruct easyrpgAnnBattleDefend2k3;

  @DMOptional @DMFXOBinding("easyrpgAnnBattleObserve2k3") @DM2LcfBinding(0xD5) @DMCXObject
  public StringR2kStruct easyrpgAnnBattleObserve2k3;

  @DMOptional @DMFXOBinding("easyrpgAnnBattleCharge2k3") @DM2LcfBinding(0xD6) @DMCXObject
  public StringR2kStruct easyrpgAnnBattleCharge2k3;

  @DMOptional @DMFXOBinding("easyrpgAnnBattleSelfdestruct2k3") @DM2LcfBinding(0xD7) @DMCXObject
  public StringR2kStruct easyrpgAnnBattleSelfdestruct2k3;

  @DMOptional @DMFXOBinding("easyrpgAnnBattleEscape2k3") @DM2LcfBinding(0xD8) @DMCXObject
  public StringR2kStruct easyrpgAnnBattleEscape2k3;

  @DMOptional @DMFXOBinding("easyrpgAnnBattleSpecialCombatBack2k3") @DM2LcfBinding(0xD9) @DMCXObject
  public StringR2kStruct easyrpgAnnBattleSpecialCombatBack2k3;

  @DMOptional @DMFXOBinding("easyrpgAnnBattleSkill2k3") @DM2LcfBinding(0xDA) @DMCXObject
  public StringR2kStruct easyrpgAnnBattleSkill2k3;

  @DMOptional @DMFXOBinding("easyrpgAnnBattleItem2k3") @DM2LcfBinding(0xDB) @DMCXObject
  public StringR2kStruct easyrpgAnnBattleItem2k3;

  public Terms(DMContext ctx) {
      super(ctx, "RPG::Terms");
  }
}
