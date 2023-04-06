/*
 * gabien-app-r48 - Editing program for various formats
 * Written starting in 2016 by contributors (see CREDITS.txt)
 * To the extent possible under law, the author(s) have dedicated all copyright and related and neighboring rights to this software to the public domain worldwide. This software is distributed without any warranty.
 * A copy of the Unlicense should have been supplied as COPYING.txt in this repository. Alternatively, you can find it at <https://unlicense.org/>.
 */

package r48.io.r2k.struct;

import r48.io.data.obj.DM2Context;
import r48.io.data.obj.DM2FXOBinding;
import r48.io.data.obj.DM2Optional;
import r48.io.data.obj.DMCXObject;
import r48.io.r2k.chunks.StringR2kStruct;
import r48.io.r2k.dm2chk.*;

public class Terms extends DM2R2kObject {
  @DM2FXOBinding("annXEncounter") @DM2LcfBinding(0x1) @DMCXObject
  public StringR2kStruct annXEncounter;

  @DM2FXOBinding("preemptiveAttack") @DM2LcfBinding(0x2) @DMCXObject
  public StringR2kStruct preemptiveAttack;

  @DM2FXOBinding("ranAwaySafely") @DM2LcfBinding(0x3) @DMCXObject
  public StringR2kStruct ranAwaySafely;

  @DM2FXOBinding("failedToEscape") @DM2LcfBinding(0x4) @DMCXObject
  public StringR2kStruct failedToEscape;

  @DM2FXOBinding("wonACombat") @DM2LcfBinding(0x5) @DMCXObject
  public StringR2kStruct wonACombat;

  @DM2FXOBinding("partyDead") @DM2LcfBinding(0x6) @DMCXObject
  public StringR2kStruct partyDead;

  @DM2FXOBinding("experience") @DM2LcfBinding(0x7) @DMCXObject
  public StringR2kStruct experience;

  @DM2FXOBinding("youFound") @DM2LcfBinding(0x8) @DMCXObject
  public StringR2kStruct youFound;

  @DM2FXOBinding("xMoney") @DM2LcfBinding(0x9) @DMCXObject
  public StringR2kStruct xMoney;

  @DM2FXOBinding("xReceived") @DM2LcfBinding(0xA) @DMCXObject
  public StringR2kStruct xReceived;

  @DM2FXOBinding("annXAttacksTheEnemy2KO") @DM2LcfBinding(0xB) @DMCXObject
  public StringR2kStruct annXAttacksTheEnemy2KO;

  @DM2FXOBinding("annCritEnemyHurt2KO") @DM2LcfBinding(0xC) @DMCXObject
  public StringR2kStruct annCritEnemyHurt2KO;

  @DM2FXOBinding("annCritPlayersHurt2KO") @DM2LcfBinding(0xD) @DMCXObject
  public StringR2kStruct annCritPlayersHurt2KO;

  @DM2FXOBinding("annXGuarding2KO") @DM2LcfBinding(0xE) @DMCXObject
  public StringR2kStruct annXGuarding2KO;

  @DM2FXOBinding("annXDoesNothing2KO") @DM2LcfBinding(0xF) @DMCXObject
  public StringR2kStruct annXDoesNothing2KO;

  @DM2FXOBinding("annXReadies2Attacks2KO") @DM2LcfBinding(0x10) @DMCXObject
  public StringR2kStruct annXReadies2Attacks2KO;

  @DM2FXOBinding("annXSelfDestructs2KO") @DM2LcfBinding(0x11) @DMCXObject
  public StringR2kStruct annXSelfDestructs2KO;

  @DM2FXOBinding("annXRunsAway2KO") @DM2LcfBinding(0x12) @DMCXObject
  public StringR2kStruct annXRunsAway2KO;

  @DM2FXOBinding("annXTransforms2KO") @DM2LcfBinding(0x13) @DMCXObject
  public StringR2kStruct annXTransforms2KO;

  @DM2FXOBinding("annEHurtForXHP2KO") @DM2LcfBinding(0x14) @DMCXObject
  public StringR2kStruct annEHurtForXHP2KO;

  @DM2FXOBinding("annXMissedE2KO") @DM2LcfBinding(0x15) @DMCXObject
  public StringR2kStruct annXMissedE2KO;

  @DM2FXOBinding("annPHurtForXHP2KO") @DM2LcfBinding(0x16) @DMCXObject
  public StringR2kStruct annPHurtForXHP2KO;

  @DM2FXOBinding("annXMissedP2KO") @DM2LcfBinding(0x17) @DMCXObject
  public StringR2kStruct annXMissedP2KO;

  @DM2FXOBinding("annXFloppedSkillA2KO") @DM2LcfBinding(0x18) @DMCXObject
  public StringR2kStruct annXFloppedSkillA2KO;

  @DM2FXOBinding("annXFloppedSkillB2KO") @DM2LcfBinding(0x19) @DMCXObject
  public StringR2kStruct annXFloppedSkillB2KO;

  @DM2FXOBinding("annXFloppedSkillC2KO") @DM2LcfBinding(0x1A) @DMCXObject
  public StringR2kStruct annXFloppedSkillC2KO;

  @DM2FXOBinding("annXDodged2KO") @DM2LcfBinding(0x1B) @DMCXObject
  public StringR2kStruct annXDodged2KO;

  @DM2FXOBinding("annUsesItem2KO") @DM2LcfBinding(0x1C) @DMCXObject
  public StringR2kStruct annUsesItem2KO;

  @DM2FXOBinding("annXRecovered2KO") @DM2LcfBinding(0x1D) @DMCXObject
  public StringR2kStruct annXRecovered2KO;

  @DM2FXOBinding("paramInc2KO") @DM2LcfBinding(0x1E) @DMCXObject
  public StringR2kStruct paramInc2KO;

  @DM2FXOBinding("paramDec2KO") @DM2LcfBinding(0x1F) @DMCXObject
  public StringR2kStruct paramDec2KO;

  @DM2FXOBinding("annXHPAbsorbedFromE2KO") @DM2LcfBinding(0x20) @DMCXObject
  public StringR2kStruct annXHPAbsorbedFromE2KO;

  @DM2FXOBinding("annXHPAbsorbedFromP2KO") @DM2LcfBinding(0x21) @DMCXObject
  public StringR2kStruct annXHPAbsorbedFromP2KO;

  @DM2FXOBinding("resistInc2KO") @DM2LcfBinding(0x22) @DMCXObject
  public StringR2kStruct resistInc2KO;

  @DM2FXOBinding("resistDec2KO") @DM2LcfBinding(0x23) @DMCXObject
  public StringR2kStruct resistDec2KO;

  @DM2FXOBinding("levelUp") @DM2LcfBinding(0x24) @DMCXObject
  public StringR2kStruct levelUp;

  @DM2FXOBinding("annLearnedX") @DM2LcfBinding(0x25) @DMCXObject
  public StringR2kStruct annLearnedX;

  @DM2FXOBinding("battleStart") @DM2LcfBinding(0x26) @DMCXObject
  public StringR2kStruct battleStart;

  @DM2FXOBinding("floatTxtMiss2k3") @DM2LcfBinding(0x27) @DMCXObject
  public StringR2kStruct floatTxtMiss2k3;

  @DM2FXOBinding("shop1Greet") @DM2LcfBinding(0x29) @DMCXObject
  public StringR2kStruct shop1Greet;

  @DM2FXOBinding("shop1Impatient") @DM2LcfBinding(0x2A) @DMCXObject
  public StringR2kStruct shop1Impatient;

  @DM2FXOBinding("shop1Buy") @DM2LcfBinding(0x2B) @DMCXObject
  public StringR2kStruct shop1Buy;

  @DM2FXOBinding("shop1Sell") @DM2LcfBinding(0x2C) @DMCXObject
  public StringR2kStruct shop1Sell;

  @DM2FXOBinding("shop1Leave") @DM2LcfBinding(0x2D) @DMCXObject
  public StringR2kStruct shop1Leave;

  @DM2FXOBinding("shop1WTBQuestion") @DM2LcfBinding(0x2E) @DMCXObject
  public StringR2kStruct shop1WTBQuestion;

  @DM2FXOBinding("shop1ICoBQuestion") @DM2LcfBinding(0x2F) @DMCXObject
  public StringR2kStruct shop1ICoBQuestion;

  @DM2FXOBinding("shop1Done") @DM2LcfBinding(0x30) @DMCXObject
  public StringR2kStruct shop1Done;

  @DM2FXOBinding("shop1WTSQuestion") @DM2LcfBinding(0x31) @DMCXObject
  public StringR2kStruct shop1WTSQuestion;

  @DM2FXOBinding("shop1ICoSQuestion") @DM2LcfBinding(0x32) @DMCXObject
  public StringR2kStruct shop1ICoSQuestion;

  @DM2FXOBinding("shop1Thanks") @DM2LcfBinding(0x33) @DMCXObject
  public StringR2kStruct shop1Thanks;

  @DM2FXOBinding("shop2Greet") @DM2LcfBinding(0x36) @DMCXObject
  public StringR2kStruct shop2Greet;

  @DM2FXOBinding("shop2Impatient") @DM2LcfBinding(0x37) @DMCXObject
  public StringR2kStruct shop2Impatient;

  @DM2FXOBinding("shop2Buy") @DM2LcfBinding(0x38) @DMCXObject
  public StringR2kStruct shop2Buy;

  @DM2FXOBinding("shop2Sell") @DM2LcfBinding(0x39) @DMCXObject
  public StringR2kStruct shop2Sell;

  @DM2FXOBinding("shop2Leave") @DM2LcfBinding(0x3A) @DMCXObject
  public StringR2kStruct shop2Leave;

  @DM2FXOBinding("shop2WTBQuestion") @DM2LcfBinding(0x3B) @DMCXObject
  public StringR2kStruct shop2WTBQuestion;

  @DM2FXOBinding("shop2ICoBQuestion") @DM2LcfBinding(0x3C) @DMCXObject
  public StringR2kStruct shop2ICoBQuestion;

  @DM2FXOBinding("shop2Done") @DM2LcfBinding(0x3D) @DMCXObject
  public StringR2kStruct shop2Done;

  @DM2FXOBinding("shop2WTSQuestion") @DM2LcfBinding(0x3E) @DMCXObject
  public StringR2kStruct shop2WTSQuestion;

  @DM2FXOBinding("shop2ICoSQuestion") @DM2LcfBinding(0x3F) @DMCXObject
  public StringR2kStruct shop2ICoSQuestion;

  @DM2FXOBinding("shop2Thanks") @DM2LcfBinding(0x40) @DMCXObject
  public StringR2kStruct shop2Thanks;

  @DM2FXOBinding("shop3Greet") @DM2LcfBinding(0x43) @DMCXObject
  public StringR2kStruct shop3Greet;

  @DM2FXOBinding("shop3Impatient") @DM2LcfBinding(0x44) @DMCXObject
  public StringR2kStruct shop3Impatient;

  @DM2FXOBinding("shop3Buy") @DM2LcfBinding(0x45) @DMCXObject
  public StringR2kStruct shop3Buy;

  @DM2FXOBinding("shop3Sell") @DM2LcfBinding(0x46) @DMCXObject
  public StringR2kStruct shop3Sell;

  @DM2FXOBinding("shop3Leave") @DM2LcfBinding(0x47) @DMCXObject
  public StringR2kStruct shop3Leave;

  @DM2FXOBinding("shop3WTBQuestion") @DM2LcfBinding(0x48) @DMCXObject
  public StringR2kStruct shop3WTBQuestion;

  @DM2FXOBinding("shop3ICoBQuestion") @DM2LcfBinding(0x49) @DMCXObject
  public StringR2kStruct shop3ICoBQuestion;

  @DM2FXOBinding("shop3Done") @DM2LcfBinding(0x4A) @DMCXObject
  public StringR2kStruct shop3Done;

  @DM2FXOBinding("shop3WTSQuestion") @DM2LcfBinding(0x4B) @DMCXObject
  public StringR2kStruct shop3WTSQuestion;

  @DM2FXOBinding("shop3ICoSQuestion") @DM2LcfBinding(0x4C) @DMCXObject
  public StringR2kStruct shop3ICoSQuestion;

  @DM2FXOBinding("shop3Thanks") @DM2LcfBinding(0x4D) @DMCXObject
  public StringR2kStruct shop3Thanks;

  @DM2FXOBinding("inn1PriceX") @DM2LcfBinding(0x50) @DMCXObject
  public StringR2kStruct inn1PriceX;

  @DM2FXOBinding("inn1XGoldToSleep") @DM2LcfBinding(0x51) @DMCXObject
  public StringR2kStruct inn1XGoldToSleep;

  @DM2FXOBinding("inn1ConfirmSleep") @DM2LcfBinding(0x52) @DMCXObject
  public StringR2kStruct inn1ConfirmSleep;

  @DM2FXOBinding("inn1SleepYes") @DM2LcfBinding(0x53) @DMCXObject
  public StringR2kStruct inn1SleepYes;

  @DM2FXOBinding("inn1SleepNo") @DM2LcfBinding(0x54) @DMCXObject
  public StringR2kStruct inn1SleepNo;

  @DM2FXOBinding("inn2PriceX") @DM2LcfBinding(0x55) @DMCXObject
  public StringR2kStruct inn2PriceX;

  @DM2FXOBinding("inn2XGoldToSleep") @DM2LcfBinding(0x56) @DMCXObject
  public StringR2kStruct inn2XGoldToSleep;

  @DM2FXOBinding("inn2ConfirmSleep") @DM2LcfBinding(0x57) @DMCXObject
  public StringR2kStruct inn2ConfirmSleep;

  @DM2FXOBinding("inn2SleepYes") @DM2LcfBinding(0x58) @DMCXObject
  public StringR2kStruct inn2SleepYes;

  @DM2FXOBinding("inn2SleepNo") @DM2LcfBinding(0x59) @DMCXObject
  public StringR2kStruct inn2SleepNo;

  @DM2FXOBinding("headOwnedItems") @DM2LcfBinding(0x5C) @DMCXObject
  public StringR2kStruct headOwnedItems;

  @DM2FXOBinding("headEquippedItems") @DM2LcfBinding(0x5D) @DMCXObject
  public StringR2kStruct headEquippedItems;

  @DM2FXOBinding("goldPostfix") @DM2LcfBinding(0x5F) @DMCXObject
  public StringR2kStruct goldPostfix;

  @DM2FXOBinding("cmdFight") @DM2LcfBinding(0x65) @DMCXObject
  public StringR2kStruct cmdFight;

  @DM2FXOBinding("cmdAuto") @DM2LcfBinding(0x66) @DMCXObject
  public StringR2kStruct cmdAuto;

  @DM2FXOBinding("cmdEscape") @DM2LcfBinding(0x67) @DMCXObject
  public StringR2kStruct cmdEscape;

  @DM2FXOBinding("cmdAttack2KO") @DM2LcfBinding(0x68) @DMCXObject
  public StringR2kStruct cmdAttack2KO;

  @DM2FXOBinding("cmdDefend2KO") @DM2LcfBinding(0x69) @DMCXObject
  public StringR2kStruct cmdDefend2KO;

  @DM2FXOBinding("cmdItemX") @DM2LcfBinding(0x6A) @DMCXObject
  public StringR2kStruct cmdItemX;

  @DM2FXOBinding("cmdSkill") @DM2LcfBinding(0x6B) @DMCXObject
  public StringR2kStruct cmdSkill;

  @DM2FXOBinding("optEquipment") @DM2LcfBinding(0x6C) @DMCXObject
  public StringR2kStruct optEquipment;

  @DM2FXOBinding("optSave") @DM2LcfBinding(0x6E) @DMCXObject
  public StringR2kStruct optSave;

  @DM2FXOBinding("optStopGame") @DM2LcfBinding(0x70) @DMCXObject
  public StringR2kStruct optStopGame;

  @DM2FXOBinding("optStart") @DM2LcfBinding(0x72) @DMCXObject
  public StringR2kStruct optStart;

  @DM2FXOBinding("optLoad") @DM2LcfBinding(0x73) @DMCXObject
  public StringR2kStruct optLoad;

  @DM2FXOBinding("optExitApp") @DM2LcfBinding(0x75) @DMCXObject
  public StringR2kStruct optExitApp;

  @DM2FXOBinding("menuStatus") @DM2LcfBinding(0x76) @DMCXObject
  public StringR2kStruct menuStatus;

  @DM2FXOBinding("menuRow") @DM2LcfBinding(0x77) @DMCXObject
  public StringR2kStruct menuRow;

  @DM2FXOBinding("menuOrder") @DM2LcfBinding(0x78) @DMCXObject
  public StringR2kStruct menuOrder;

  @DM2FXOBinding("menuWaitOn") @DM2LcfBinding(0x79) @DMCXObject
  public StringR2kStruct menuWaitOn;

  @DM2FXOBinding("menuWaitOff") @DM2LcfBinding(0x7A) @DMCXObject
  public StringR2kStruct menuWaitOff;

  @DM2FXOBinding("batStatLevel") @DM2LcfBinding(0x7B) @DMCXObject
  public StringR2kStruct batStatLevel;

  @DM2FXOBinding("batStatHP") @DM2LcfBinding(0x7C) @DMCXObject
  public StringR2kStruct batStatHP;

  @DM2FXOBinding("batStatSP") @DM2LcfBinding(0x7D) @DMCXObject
  public StringR2kStruct batStatSP;

  @DM2FXOBinding("batStatNoStateFx") @DM2LcfBinding(0x7E) @DMCXObject
  public StringR2kStruct batStatNoStateFx;

  @DM2FXOBinding("exp2ch") @DM2LcfBinding(0x7F) @DMCXObject
  public StringR2kStruct exp2ch;

  @DM2FXOBinding("lvl2ch") @DM2LcfBinding(0x80) @DMCXObject
  public StringR2kStruct lvl2ch;

  @DM2FXOBinding("hp2ch") @DM2LcfBinding(0x81) @DMCXObject
  public StringR2kStruct hp2ch;

  @DM2FXOBinding("sp2ch") @DM2LcfBinding(0x82) @DMCXObject
  public StringR2kStruct sp2ch;

  @DM2FXOBinding("spCost") @DM2LcfBinding(0x83) @DMCXObject
  public StringR2kStruct spCost;

  @DM2FXOBinding("statAtk") @DM2LcfBinding(0x84) @DMCXObject
  public StringR2kStruct statAtk;

  @DM2FXOBinding("statDef") @DM2LcfBinding(0x85) @DMCXObject
  public StringR2kStruct statDef;

  @DM2FXOBinding("statSpi") @DM2LcfBinding(0x86) @DMCXObject
  public StringR2kStruct statSpi;

  @DM2FXOBinding("statAgi") @DM2LcfBinding(0x87) @DMCXObject
  public StringR2kStruct statAgi;

  @DM2FXOBinding("equipWeapon") @DM2LcfBinding(0x88) @DMCXObject
  public StringR2kStruct equipWeapon;

  @DM2FXOBinding("equipShield") @DM2LcfBinding(0x89) @DMCXObject
  public StringR2kStruct equipShield;

  @DM2FXOBinding("equipArmour") @DM2LcfBinding(0x8A) @DMCXObject
  public StringR2kStruct equipArmour;

  @DM2FXOBinding("equipHelmet") @DM2LcfBinding(0x8B) @DMCXObject
  public StringR2kStruct equipHelmet;

  @DM2FXOBinding("equipAccessory") @DM2LcfBinding(0x8C) @DMCXObject
  public StringR2kStruct equipAccessory;

  @DM2FXOBinding("saveSlot") @DM2LcfBinding(0x92) @DMCXObject
  public StringR2kStruct saveSlot;

  @DM2FXOBinding("loadSlot") @DM2LcfBinding(0x93) @DMCXObject
  public StringR2kStruct loadSlot;

  @DM2FXOBinding("file") @DM2LcfBinding(0x94) @DMCXObject
  public StringR2kStruct file;

  @DM2FXOBinding("exitGame") @DM2LcfBinding(0x97) @DMCXObject
  public StringR2kStruct exitGame;

  @DM2FXOBinding("yes") @DM2LcfBinding(0x98) @DMCXObject
  public StringR2kStruct yes;

  @DM2FXOBinding("no") @DM2LcfBinding(0x99) @DMCXObject
  public StringR2kStruct no;

  @DM2Optional @DM2FXOBinding("easyrpgItemNumberSeparator") @DM2LcfBinding(0xC8) @DMCXObject
  public StringR2kStruct easyrpgItemNumberSeparator;

  @DM2Optional @DM2FXOBinding("easyrpgSkillCostSeparator") @DM2LcfBinding(0xC9) @DMCXObject
  public StringR2kStruct easyrpgSkillCostSeparator;

  @DM2Optional @DM2FXOBinding("easyrpgEquipmentArrow") @DM2LcfBinding(0xCA) @DMCXObject
  public StringR2kStruct easyrpgEquipmentArrow;

  @DM2Optional @DM2FXOBinding("easyrpgStatusSceneName") @DM2LcfBinding(0xCB) @DMCXObject
  public StringR2kStruct easyrpgStatusSceneName;

  @DM2Optional @DM2FXOBinding("easyrpgStatusSceneClass") @DM2LcfBinding(0xCC) @DMCXObject
  public StringR2kStruct easyrpgStatusSceneClass;

  @DM2Optional @DM2FXOBinding("easyrpgStatusSceneTitle") @DM2LcfBinding(0xCD) @DMCXObject
  public StringR2kStruct easyrpgStatusSceneTitle;

  @DM2Optional @DM2FXOBinding("easyrpgStatusSceneCondition") @DM2LcfBinding(0xCE) @DMCXObject
  public StringR2kStruct easyrpgStatusSceneCondition;

  @DM2Optional @DM2FXOBinding("easyrpgStatusSceneFront") @DM2LcfBinding(0xCF) @DMCXObject
  public StringR2kStruct easyrpgStatusSceneFront;

  @DM2Optional @DM2FXOBinding("easyrpgStatusSceneBack") @DM2LcfBinding(0xD0) @DMCXObject
  public StringR2kStruct easyrpgStatusSceneBack;

  @DM2Optional @DM2FXOBinding("easyrpgOrderSceneConfirm") @DM2LcfBinding(0xD1) @DMCXObject
  public StringR2kStruct easyrpgOrderSceneConfirm;

  @DM2Optional @DM2FXOBinding("easyrpgOrderSceneRedo") @DM2LcfBinding(0xD2) @DMCXObject
  public StringR2kStruct easyrpgOrderSceneRedo;

  @DM2Optional @DM2FXOBinding("easyrpgAnnBattleDoubleAttack2k3") @DM2LcfBinding(0xD3) @DMCXObject
  public StringR2kStruct easyrpgAnnBattleDoubleAttack2k3;

  @DM2Optional @DM2FXOBinding("easyrpgAnnBattleDefend2k3") @DM2LcfBinding(0xD4) @DMCXObject
  public StringR2kStruct easyrpgAnnBattleDefend2k3;

  @DM2Optional @DM2FXOBinding("easyrpgAnnBattleObserve2k3") @DM2LcfBinding(0xD5) @DMCXObject
  public StringR2kStruct easyrpgAnnBattleObserve2k3;

  @DM2Optional @DM2FXOBinding("easyrpgAnnBattleCharge2k3") @DM2LcfBinding(0xD6) @DMCXObject
  public StringR2kStruct easyrpgAnnBattleCharge2k3;

  @DM2Optional @DM2FXOBinding("easyrpgAnnBattleSelfdestruct2k3") @DM2LcfBinding(0xD7) @DMCXObject
  public StringR2kStruct easyrpgAnnBattleSelfdestruct2k3;

  @DM2Optional @DM2FXOBinding("easyrpgAnnBattleEscape2k3") @DM2LcfBinding(0xD8) @DMCXObject
  public StringR2kStruct easyrpgAnnBattleEscape2k3;

  @DM2Optional @DM2FXOBinding("easyrpgAnnBattleSpecialCombatBack2k3") @DM2LcfBinding(0xD9) @DMCXObject
  public StringR2kStruct easyrpgAnnBattleSpecialCombatBack2k3;

  @DM2Optional @DM2FXOBinding("easyrpgAnnBattleSkill2k3") @DM2LcfBinding(0xDA) @DMCXObject
  public StringR2kStruct easyrpgAnnBattleSkill2k3;

  @DM2Optional @DM2FXOBinding("easyrpgAnnBattleItem2k3") @DM2LcfBinding(0xDB) @DMCXObject
  public StringR2kStruct easyrpgAnnBattleItem2k3;

  public Terms(DM2Context ctx) {
      super(ctx, "RPG::Terms");
  }
}
