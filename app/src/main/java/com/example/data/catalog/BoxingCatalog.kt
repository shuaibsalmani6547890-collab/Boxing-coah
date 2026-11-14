package com.example.data.catalog

import com.example.data.model.*
import java.util.UUID

object BoxingCatalog {

    val techniques: List<BoxingTechnique> = listOf(
        BoxingTechnique(
            id = "stance_orthodox",
            nameHi = "Orthodox Stance",
            nameEn = "Orthodox Stance",
            categoryHi = "Stance",
            categoryEn = "Stance",
            animType = TechniqueAnimType.STANCE_ORTHODOX,
            stepsHi = listOf(
                "Baayan pair (left) aage rakho, daayan pair (right) peeche 45° angle par.",
                "Pair shoulder-width jitne chauda rakho, ghutne halka bend.",
                "Pichhle pair ki heel thodi uthi hui (balls of feet par weight).",
                "Dono haath gaalon/chin ke paas, elbows ribs se chipke aur chin tucked."
            ),
            stepsEn = listOf(
                "Lead with left foot forward, rear foot angled 45° back.",
                "Keep feet shoulder-width apart with soft bent knees.",
                "Stay on the balls of your feet with rear heel slightly lifted.",
                "Hands high protecting cheekbones, elbows tucked to ribs, chin down."
            ),
            mistakesHi = listOf(
                "Pair ek line me rakhna (balance kharab hota hai).",
                "Pair bahut paas ya bahut door rakhna.",
                "Chin upar utha ke rakhna aur haath chest pe girana."
            ),
            mistakesEn = listOf(
                "Standing in a tightrope single line (kills lateral balance).",
                "Feet too close together or overly wide.",
                "Lifting chin high and dropping hands below chin."
            )
        ),
        BoxingTechnique(
            id = "stance_southpaw",
            nameHi = "Southpaw Stance",
            nameEn = "Southpaw Stance",
            categoryHi = "Stance",
            categoryEn = "Stance",
            animType = TechniqueAnimType.STANCE_SOUTHPAW,
            stepsHi = listOf(
                "Daayan pair (right) aage aur baayan pair (left) peeche rakho.",
                "Right hand lead guard aur jab ke liye, left hand power cross ke liye.",
                "Weight 50-50 dono pairo par aur chin front shoulder ke peeche tuck."
            ),
            stepsEn = listOf(
                "Lead with right foot forward and left foot back.",
                "Right hand acts as lead jab; left hand loads the power cross.",
                "Keep 50-50 weight distribution and chin tucked behind lead shoulder."
            ),
            mistakesHi = listOf(
                "Orthodox opponent ke front foot ke andar pair rakhna.",
                "Rear hand ko hip ke paas latka dena."
            ),
            mistakesEn = listOf(
                "Stepping inside an orthodox opponent's lead foot.",
                "Dropping the rear left hand near the waist."
            )
        ),
        BoxingTechnique(
            id = "jab",
            nameHi = "Jab (1)",
            nameEn = "Jab (1)",
            categoryHi = "Punch",
            categoryEn = "Punch",
            animType = TechniqueAnimType.JAB,
            stepsHi = listOf(
                "Lead haath ko seedhe line me nose/chin height se aage fire karo.",
                "Impact par fist rotate karo (thumb neeche), elbow poori kholo.",
                "Lead shoulder utha ke chin cover karo; doosra haath cheek par chipka rahe.",
                "Seedhe line me turant wapas guard pe lao."
            ),
            stepsEn = listOf(
                "Snap the lead fist straight out from the cheek toward eye level.",
                "Rotate wrist at full extension with lead shoulder shielding your chin.",
                "Keep rear hand glued firmly to your jaw as guard.",
                "Retract along the exact same line back to guard."
            ),
            mistakesHi = listOf(
                "Punch se pehle haath peeche kheench ke telegraph karna.",
                "Rear guard hand neeche gira dena.",
                "Aage jhuk kar balance khona."
            ),
            mistakesEn = listOf(
                "Pulling the hand back before throwing (telegraphing).",
                "Dropping the rear guard hand.",
                "Over-leaning torso past lead knee."
            )
        ),
        BoxingTechnique(
            id = "cross",
            nameHi = "Cross (2)",
            nameEn = "Cross (2)",
            categoryHi = "Punch",
            categoryEn = "Punch",
            animType = TechniqueAnimType.CROSS,
            stepsHi = listOf(
                "Pichhle pair ke ball par pivot karo (heel bahar ghumao).",
                "Hip aur rear shoulder aage rotate karte hue rear haath seedha maaro.",
                "Lead haath wapas face ke paas high guard me rakhna zaroori hai.",
                "Fist extend hone ke turant baad waist & hand reset karo."
            ),
            stepsEn = listOf(
                "Pivot on the ball of the rear foot, driving rear hip forward.",
                "Rotate shoulders fully as rear fist shoots straight down the pipe.",
                "Snap lead hand back tight to your cheek.",
                "Recover stance quickly after full extension."
            ),
            mistakesHi = listOf(
                "Sirf haath se maarna aur hip/toe pivot na karna.",
                "Lead haath neeche girana jab cross aage jaaye."
            ),
            mistakesEn = listOf(
                "Arm-punching without rotating rear hip and foot.",
                "Dropping lead guard during the cross."
            )
        ),
        BoxingTechnique(
            id = "hook",
            nameHi = "Lead Hook (3)",
            nameEn = "Lead Hook (3)",
            categoryHi = "Punch",
            categoryEn = "Punch",
            animType = TechniqueAnimType.HOOK,
            stepsHi = listOf(
                "Lead elbow ko 90° bend rakho, forearm floor ke parallel.",
                "Lead foot par pivot aur hip ko peeche ki taraf ghumao (weight shift).",
                "Short tight arc me cheek-level target par maaro.",
                "Doosra haath chin par chipka rahe."
            ),
            stepsEn = listOf(
                "Bend lead elbow to ~90° with forearm parallel to floor.",
                "Pivot lead foot and whip hips/shoulders horizontally.",
                "Keep the arc tight—never swing wide.",
                "Keep rear hand glued to chin throughout."
            ),
            mistakesHi = listOf(
                "Haath peeche le jaake bada 'haymaker' swing maarna.",
                "Elbow neeche rakhna ya hook maarte waqt pivot na karna."
            ),
            mistakesEn = listOf(
                "Winding up wide like a haymaker.",
                "Dropping elbow below wrist or failing to pivot lead foot."
            )
        ),
        BoxingTechnique(
            id = "uppercut",
            nameHi = "Uppercut (5/6)",
            nameEn = "Uppercut (5/6)",
            categoryHi = "Punch",
            categoryEn = "Punch",
            animType = TechniqueAnimType.UPPERCUT,
            stepsHi = listOf(
                "Ghutno se halka neeche dip karo aur shoulder rotate karo.",
                "Palm apni taraf rakhte hue neeche se upar explosive drive maaro.",
                "Power legs aur hips se upar transfer karo, arm sirf 90° lock rahe.",
                "Punch ke turant baad haath wapas guard me lao."
            ),
            stepsEn = listOf(
                "Dip knees slightly and load weight onto the punching-side leg.",
                "Drive upward with legs and hips, palm facing you at 90° elbow bend.",
                "Stop at chin height—don't over-swing past your own eyes.",
                "Snap back immediately to high guard."
            ),
            mistakesHi = listOf(
                "Haath ko zameen tak neeche gira dena.",
                "Sirf bicep se uthana, legs use na karna."
            ),
            mistakesEn = listOf(
                "Dropping the hand down near the hip before rising.",
                "Arm-lifting without knee dip and hip drive."
            )
        ),
        BoxingTechnique(
            id = "slip",
            nameHi = "Slip (Left/Right)",
            nameEn = "Slip (Left/Right)",
            categoryHi = "Defense",
            categoryEn = "Defense",
            animType = TechniqueAnimType.SLIP,
            stepsHi = listOf(
                "Haath face par hi rakho aur aankhein samne opponent par.",
                "Kamar se nahi, ghutno aur shoulder twist se sir ko 1-2 inch side hatao.",
                "Slip karke turant counter Cross ya Hook ke liye load ho jao."
            ),
            stepsEn = listOf(
                "Keep hands glued to cheeks and eyes locked forward.",
                "Bend knees slightly and rotate shoulder to move head just off center.",
                "Stay loaded on that leg to fire an instant counter."
            ),
            mistakesHi = listOf(
                "Neeche ya side me bahut zyada jhukna.",
                "Slip karte waqt guard neeche gira dena."
            ),
            mistakesEn = listOf(
                "Bending too low at the waist and losing balance.",
                "Dropping hands while slipping."
            )
        ),
        BoxingTechnique(
            id = "bob_weave",
            nameHi = "Bob & Weave",
            nameEn = "Bob & Weave",
            categoryHi = "Defense",
            categoryEn = "Defense",
            animType = TechniqueAnimType.BOB_WEAVE,
            stepsHi = listOf(
                "Hook se bachne ke liye ghutno se neeche squat (U-shape motion) karo.",
                "Head ko U-shape me ek side se doosri side le jaake upar aao.",
                "Back seedhi rakho aur aankhein hamesha samne."
            ),
            stepsEn = listOf(
                "Bend knees to dip straight under an incoming hook.",
                "Trace a smooth 'U' shape with your head to the opposite side.",
                "Keep spine upright and eyes up—never look at the floor."
            ),
            mistakesHi = listOf(
                "Ghutne bend karne ki jagah kamar jhuka ke floor dekhna."
            ),
            mistakesEn = listOf(
                "Bending at the lower back and looking down at the mat."
            )
        ),
        BoxingTechnique(
            id = "parry",
            nameHi = "Parry",
            nameEn = "Parry",
            categoryHi = "Defense",
            categoryEn = "Defense",
            animType = TechniqueAnimType.PARRY,
            stepsHi = listOf(
                "Rear open glove se aate hue jab ko halka sa side tap (deflect) karo.",
                "Haath face se 2-3 inch se zyada aage mat le jao.",
                "Parry karte hi lead jab ya cross counter maaro."
            ),
            stepsEn = listOf(
                "Use a short, crisp palm tap to redirect a straight punch offline.",
                "Keep the parry small—never reach far out from your face.",
                "Immediately fire a counter jab or cross."
            ),
            mistakesHi = listOf(
                "Bada slap maar ke apna face poora khol dena."
            ),
            mistakesEn = listOf(
                "Reaching way out to swat, leaving your jaw wide open."
            )
        ),
        BoxingTechnique(
            id = "step_drag",
            nameHi = "Step-Drag Footwork",
            nameEn = "Step-Drag Footwork",
            categoryHi = "Footwork",
            categoryEn = "Footwork",
            animType = TechniqueAnimType.STEP_DRAG,
            stepsHi = listOf(
                "Aage badhna hai to pehle lead pair aage rakho, phir rear pair drag karo.",
                "Peeche jaana hai to pehle rear pair peeche, phir lead pair drag karo.",
                "Pairon ke beech ka distance hamesha barabar (shoulder width) rakho."
            ),
            stepsEn = listOf(
                "Moving forward: step lead foot first, then slide rear foot.",
                "Moving backward: step rear foot first, then slide lead foot.",
                "Maintain exact shoulder-width stance spacing after every step."
            ),
            mistakesHi = listOf(
                "Galat pair pehle utha ke pair cross kar lena."
            ),
            mistakesEn = listOf(
                "Stepping with the wrong foot first and crossing legs."
            )
        ),
        BoxingTechnique(
            id = "pivot",
            nameHi = "Pivot (Angle Change)",
            nameEn = "Pivot (Angle Change)",
            categoryHi = "Footwork",
            categoryEn = "Footwork",
            animType = TechniqueAnimType.PIVOT,
            stepsHi = listOf(
                "Lead pair ke ball par weight rakho aur anchor banao.",
                "Rear pair ko 45°–90° arc me ghumao jab tak naya angle na ban jaye.",
                "Guard high rakho aur pivot ke baad hook/cross ready rakho."
            ),
            stepsEn = listOf(
                "Anchor weight onto the ball of your lead foot.",
                "Swing rear foot 45°–90° around to create a dominant side angle.",
                "Stay in guard ready to counter immediately."
            ),
            mistakesHi = listOf(
                "Flat heel par ghumna ya balance ke liye pair cross karna."
            ),
            mistakesEn = listOf(
                "Pivoting on flat heels or letting stance collapse."
            )
        ),
        BoxingTechnique(
            id = "circle_step",
            nameHi = "Circle Step",
            nameEn = "Circle Step",
            categoryHi = "Footwork",
            categoryEn = "Footwork",
            animType = TechniqueAnimType.CIRCLE_STEP,
            stepsHi = listOf(
                "Left jaana ho to left foot pehle side step karo, phir right follow kare.",
                "Right jaana ho to right foot pehle side step karo, phir left follow kare.",
                "Balls of feet par light bounce rakho aur distance maintain karo."
            ),
            stepsEn = listOf(
                "Circling left: lead with left foot, follow with right.",
                "Circling right: lead with right foot, follow with left.",
                "Keep light rhythmic bounce on balls of feet."
            ),
            mistakesHi = listOf(
                "Heels zameen pe chipka ke chalna ya pair milana."
            ),
            mistakesEn = listOf(
                "Walking flat-footed or clicking feet together."
            )
        )
    )

    val presetCombos: List<BoxingCombo> = listOf(
        BoxingCombo("c12", "1-2", "1-2 (Jab - Cross)", "1-2 (Jab - Cross)", listOf("JAB", "CROSS"), "Jab, Cross!", "Jab, Cross!", "Beginner"),
        BoxingCombo("c123", "1-2-3", "1-2-3 (Jab - Cross - Hook)", "1-2-3 (Jab - Cross - Hook)", listOf("JAB", "CROSS", "LEAD_HOOK"), "Jab, Cross, Hook!", "Jab, Cross, Hook!", "Beginner"),
        BoxingCombo("c112", "1-1-2", "1-1-2 (Double Jab - Cross)", "1-1-2 (Double Jab - Cross)", listOf("JAB", "JAB", "CROSS"), "Jab, Jab, Cross!", "Double Jab, Cross!", "Beginner"),
        BoxingCombo("c1232", "1-2-3-2", "1-2-3-2 (Four Punch Flow)", "1-2-3-2 (Four Punch Flow)", listOf("JAB", "CROSS", "LEAD_HOOK", "CROSS"), "Jab, Cross, Hook, Cross!", "Jab, Cross, Hook, Cross!", "Intermediate"),
        BoxingCombo("c1252", "1-2-5-2", "1-2-5-2 (Jab - Cross - Uppercut - Cross)", "1-2-5-2 (Jab - Cross - Uppercut - Cross)", listOf("JAB", "CROSS", "UPPERCUT", "CROSS"), "Jab, Cross, Uppercut, Cross!", "Jab, Cross, Uppercut, Cross!", "Intermediate"),
        BoxingCombo("c232", "2-3-2", "2-3-2 (Cross - Hook - Cross)", "2-3-2 (Cross - Hook - Cross)", listOf("CROSS", "LEAD_HOOK", "CROSS"), "Cross, Hook, Cross!", "Cross, Hook, Cross!", "Champ")
    )

    val exercises: List<BoxingExercise> = listOf(
        // SECTION 1: RING & BAG DRILLS
        BoxingExercise(
            id = "shadow_boxing",
            category = ExerciseCategory.RING_BAG,
            motionType = ExerciseMotionType.SHADOW_BOXING,
            nameHi = "Shadow Boxing Flow",
            nameEn = "Shadow Boxing Flow",
            muscleHi = "Shoulders, Core, Footwork",
            muscleEn = "Shoulders, Core, Footwork",
            howToHi = "High guard ke sath combinations aur footwork flow karo.",
            howToEn = "Flow combinations with crisp footwork, head movement, and tight guard.",
            stepsHi = listOf("Orthodox/Southpaw stance me high guard lo.", "1-2, 1-2-3 combos hawa me sharp snap karo.", "Har combo ke baad slip ya step-drag pivot karo."),
            stepsEn = listOf("Lock into tight high guard stance.", "Snap 1-2 and 1-2-3 combinations in clean straight lines.", "Exit every combo with a slip or lateral pivot."),
            mistakesHi = listOf("Punch ke baad haath chest par girana.", "Pair ek jagah jamana (zero footwork)."),
            mistakesEn = listOf("Dropping hands after punches.", "Standing flat-footed without head movement."),
            boxingBenefitHi = "Ring me speed, muscle memory aur rhythm bina chot ke sharp karta hai.",
            boxingBenefitEn = "Builds fight speed, combo muscle memory, and ring rhythm.",
            tempoCue = "Fast Snap • 100% Guard Return • Exhale 'Shh'",
            restSeconds = 60,
            baseSets = 4,
            baseReps = "3 min",
            equipment = Equipment.NONE
        ),
        BoxingExercise(
            id = "heavy_bag",
            category = ExerciseCategory.RING_BAG,
            motionType = ExerciseMotionType.HEAVY_BAG,
            nameHi = "Heavy Bag Power Rounds",
            nameEn = "Heavy Bag Power Rounds",
            muscleHi = "Shoulders, Back, Core, Legs",
            muscleEn = "Shoulders, Back, Core, Legs",
            howToHi = "Bag ke upar range maintain karke crisp power shots maaro.",
            howToEn = "Keep proper arm-length range and snap punches without pushing the bag.",
            stepsHi = listOf("Jab se distance naapo — arm poora khulna chahiye.", "Hip aur rear toe pivot karke Cross & Hook land karo.", "Punch ke baad turant pivot karke angle change karo."),
            stepsEn = listOf("Measure distance with a stiff lead jab.", "Drive hip rotation through Cross and Hook.", "Circle out or pivot after every 3-punch exchange."),
            mistakesHi = listOf("Bag ko dhakka dena (pushing) instead of snapping.", "Bag ke bilkul chipak ke khade rehna."),
            mistakesEn = listOf("Pushing the bag instead of snapping punches.", "Smothering your own reach too close to the bag."),
            boxingBenefitHi = "Real knockout power, wrist alignment aur shoulder endurance banata hai.",
            boxingBenefitEn = "Develops knockout power, wrist alignment, and shoulder stamina.",
            tempoCue = "Explosive Impact • Instant Snap Back",
            restSeconds = 60,
            baseSets = 5,
            baseReps = "3 min",
            equipment = Equipment.HEAVY_BAG
        ),
        BoxingExercise(
            id = "speed_bag",
            category = ExerciseCategory.RING_BAG,
            motionType = ExerciseMotionType.SPEED_BAG,
            nameHi = "Speed Bag / Air Rhythm",
            nameEn = "Speed Bag / Air Rhythm",
            muscleHi = "Front Delts, Forearms, Timing",
            muscleEn = "Front Delts, Forearms, Timing",
            howToHi = "Eye-level par haath rakh kar rhythmic circular punches ghumao.",
            howToEn = "Keep hands at eye height cycling rapid small circular punches.",
            stepsHi = listOf("Elbows upar aur fists chin/eye level par rakho.", "Chhote tight circles me right-right-left-left rhythm banao.", "30 sec fast burst, phir 30 sec steady speed."),
            stepsEn = listOf("Hold elbows high with fists at eye level.", "Circle fists rapidly in tight rhythmic beats.", "Alternate 30s sprint pace and 30s control pace."),
            mistakesHi = listOf("Elbows neeche girana ya shoulders tight kar lena."),
            mistakesEn = listOf("Dropping elbows or tensing up neck traps."),
            boxingBenefitHi = "Round 8-12 me bhi haath upar rakhne ki shoulder endurance deta hai.",
            boxingBenefitEn = "Keeps your guard high in championship rounds without shoulder burn.",
            tempoCue = "Continuous Steady Rhythm • High Elbows",
            restSeconds = 45,
            baseSets = 4,
            baseReps = "2 min",
            equipment = Equipment.NONE
        ),
        BoxingExercise(
            id = "double_end_bag",
            category = ExerciseCategory.RING_BAG,
            motionType = ExerciseMotionType.DOUBLE_END_BAG,
            nameHi = "Reflex Slip & Counter Drill",
            nameEn = "Reflex Slip & Counter Drill",
            muscleHi = "Reaction, Core, Neck, Shoulders",
            muscleEn = "Reaction, Core, Neck, Shoulders",
            howToHi = "Tez jab maaro, slip karo aur turant cross-hook counter fire karo.",
            howToEn = "Snap quick jab, slip offline, and fire immediate cross-hook counter.",
            stepsHi = listOf("1-2 fast maaro aur haath face par chipkao.", "Left slip karke 3-2 counter lagao.", "Head hamesha center-line se hatate raho."),
            stepsEn = listOf("Fire 1-2 and snap hands straight to guard.", "Slip left immediately and counter with 3-2.", "Keep head moving off the centerline."),
            mistakesHi = listOf("Punch ke baad sir ek jagah seedha chhod dena."),
            mistakesEn = listOf("Leaving head stationary after throwing."),
            boxingBenefitHi = "Accuracy aur incoming punches se bachne ka pro reflex sharp karta hai.",
            boxingBenefitEn = "Sharpens pinpoint punch accuracy and defensive head reflexes.",
            tempoCue = "Hit • Slip • Counter in 1 Second",
            restSeconds = 45,
            baseSets = 4,
            baseReps = "2 min",
            equipment = Equipment.NONE
        ),
        BoxingExercise(
            id = "skipping",
            category = ExerciseCategory.RING_BAG,
            motionType = ExerciseMotionType.SKIPPING,
            nameHi = "Boxing Jump Rope (Skipping)",
            nameEn = "Boxing Jump Rope (Skipping)",
            muscleHi = "Calves, Ankles, Wrists, Heart",
            muscleEn = "Calves, Ankles, Wrists, Heart",
            howToHi = "Balls of feet par chhote hops, wrists se rope ghumao.",
            howToEn = "Light jumps on balls of feet turning rope strictly from wrists.",
            stepsHi = listOf("Elbows waist ke paas rakho, sirf wrists ghumao.", "Zameen se sirf 1 inch upar bounce karo (Boxer step).", "Har 30 sec me alternate foot shift karo."),
            stepsEn = listOf("Pin elbows near ribs and rotate purely from wrists.", "Jump barely 1 inch off floor using the boxer shuffle.", "Shift weight side-to-side every few beats."),
            mistakesHi = listOf("Bahut uncha koodna ya poore arms hilana.", "Heels zameen pe patakna."),
            mistakesEn = listOf("Jumping too high or swinging whole arms.", "Landing heavy on flat heels."),
            boxingBenefitHi = "Ring me 'Ali Shuffle' jaise light footwork aur 3x stamina banata hai.",
            boxingBenefitEn = "Builds light bouncy footwork and elite cardiovascular rhythm.",
            tempoCue = "120-140 BPM Light Boxer Shuffle",
            restSeconds = 45,
            baseSets = 4,
            baseReps = "3 min",
            equipment = Equipment.SKIPPING_ROPE
        ),

        // SECTION 2: CARDIO & CONDITIONING
        BoxingExercise(
            id = "burpees",
            category = ExerciseCategory.CARDIO_CONDITIONING,
            motionType = ExerciseMotionType.BURPEES,
            nameHi = "Explosive Fight Burpees",
            nameEn = "Explosive Fight Burpees",
            muscleHi = "Full Body, Chest, Legs, Lungs",
            muscleEn = "Full Body, Chest, Legs, Lungs",
            howToHi = "Squat down, plank kick-back, chest touch, explosive jump.",
            howToEn = "Drop to plank, chest down, snap feet forward, explode upward.",
            stepsHi = listOf("Haath zameen par rakh ke pair peeche plank me feko.", "Ek sharp push-up maarke pair aage kheecho.", "Upar explosive jump karke high guard me land karo."),
            stepsEn = listOf("Plant hands and kick feet back into tight plank.", "Perform a crisp push-up and snap feet forward.", "Jump explosively and land back in boxing guard."),
            mistakesHi = listOf("Plank me kamar neeche latka dena.", "Saans rok lena."),
            mistakesEn = listOf("Letting lower back sag in plank.", "Holding breath during reps."),
            boxingBenefitHi = "High-intensity flurry ke baad jaldi recover karne ki chamta badhata hai.",
            boxingBenefitEn = "Boosts anaerobic recovery after heavy punching flurries.",
            tempoCue = "Fast Drop • Explosive Jump • Steady Breath",
            restSeconds = 60,
            baseSets = 4,
            baseReps = "15 reps",
            equipment = Equipment.NONE
        ),
        BoxingExercise(
            id = "running",
            category = ExerciseCategory.CARDIO_CONDITIONING,
            motionType = ExerciseMotionType.RUNNING_SPRINTS,
            nameHi = "Roadwork / HIIT Sprints",
            nameEn = "Roadwork / HIIT Sprints",
            muscleHi = "Heart, Lungs, Quads, Calves",
            muscleEn = "Heart, Lungs, Quads, Calves",
            howToHi = "30 sec full sprint + 30 sec walk intervals ya 20 min steady run.",
            howToEn = "30s all-out sprint / 30s active walk intervals or steady roadwork.",
            stepsHi = listOf("5 min light warm-up jog se shuru karo.", "30 sec 90% speed sprint, phir 30 sec fast walk.", "Arms cheek-to-hip 90° swing karte raho."),
            stepsEn = listOf("Warm up with 5 min easy jog.", "Sprint 30s at 90% effort, walk 30s to recover.", "Drive elbows cleanly cheek-to-hip."),
            mistakesHi = listOf("Warm-up ke bina seedha sprint karna.", "Heels par heavy stride maarna."),
            mistakesEn = listOf("Sprinting cold without warm-up.", "Over-striding onto heels."),
            boxingBenefitHi = "Asli 'Gas Tank' — last round me bhi thakne nahi deta.",
            boxingBenefitEn = "Builds championship aerobic base so you never gas out late.",
            tempoCue = "30s Sprint / 30s Walk × 10 Rounds",
            restSeconds = 30,
            baseSets = 1,
            baseReps = "20-25 min",
            equipment = Equipment.NONE
        ),
        BoxingExercise(
            id = "mountain_climbers",
            category = ExerciseCategory.CARDIO_CONDITIONING,
            motionType = ExerciseMotionType.MOUNTAIN_CLIMBERS,
            nameHi = "Fast Mountain Climbers",
            nameEn = "Fast Mountain Climbers",
            muscleHi = "Core, Hip Flexors, Shoulders",
            muscleEn = "Core, Hip Flexors, Shoulders",
            howToHi = "High plank me ghutne tez chest ki taraf drive karo.",
            howToEn = "Hold crisp plank and drive knees rapidly toward chest.",
            stepsHi = listOf("Hands bilkul shoulders ke neeche rakho.", "Hips flat rakhte hue alternate knees chest tak lao.", "30-45 sec non-stop sprint speed rakho."),
            stepsEn = listOf("Stack wrists directly under shoulders.", "Keep hips level while driving knees fast.", "Maintain nonstop sprint cadence for 40s."),
            mistakesHi = listOf("Hips bahut upar utha lena."),
            mistakesEn = listOf("Piking hips up toward the ceiling."),
            boxingBenefitHi = "Core endurance aur guard stability ko fatigue me bhi lock rakhta hai.",
            boxingBenefitEn = "Builds shoulder + core stamina under high heart rate.",
            tempoCue = "Rapid Alternating Knees • Flat Back",
            restSeconds = 45,
            baseSets = 4,
            baseReps = "40 sec",
            equipment = Equipment.NONE
        ),
        BoxingExercise(
            id = "agility_footwork",
            category = ExerciseCategory.CARDIO_CONDITIONING,
            motionType = ExerciseMotionType.AGILITY_FOOTWORK,
            nameHi = "Shadow Ring Footwork Ladder",
            nameEn = "Shadow Ring Footwork Drill",
            muscleHi = "Calves, Ankles, Balance, Coordination",
            muscleEn = "Calves, Ankles, Balance, Coordination",
            howToHi = "Aage-peeche step-drag, left-right circle aur 90° pivot drills.",
            howToEn = "Continuous step-drag, lateral circles, and 90° pivots in guard.",
            stepsHi = listOf("Forward step-drag 3 kadam, phir 1-2 jab-cross.", "Left pivot 90° ghum ke naya angle lo.", "Kabhi bhi dono pair aapas me cross na hone do."),
            stepsEn = listOf("Step-drag forward 3 steps, snap 1-2.", "Pivot 90° off lead ball of foot.", "Keep feet shoulder-width at all times."),
            mistakesHi = listOf("Pair cross karna ya heels zameen pe chipkana."),
            mistakesEn = listOf("Crossing ankles or walking flat-footed."),
            boxingBenefitHi = "Ring control aur opponent ke power hand se door rehne ka hunar.",
            boxingBenefitEn = "Teaches effortless ring generalship and angle cutting.",
            tempoCue = "Light Toes • Sharp Angles • Hands High",
            restSeconds = 45,
            baseSets = 4,
            baseReps = "2 min",
            equipment = Equipment.NONE
        ),

        // SECTION 3: UPPER BODY & KNOCKOUT POWER
        BoxingExercise(
            id = "pushups",
            category = ExerciseCategory.UPPER_POWER,
            motionType = ExerciseMotionType.PUSH_UPS,
            nameHi = "Explosive Boxing Push-Ups",
            nameEn = "Explosive Boxing Push-Ups",
            muscleHi = "Chest, Triceps, Front Shoulders",
            muscleEn = "Chest, Triceps, Front Shoulders",
            howToHi = "Elbows 45° andar rakho, neeche control aur upar tez explosive push.",
            howToEn = "Tuck elbows at 45°, lower cleanly, explode upward.",
            stepsHi = listOf("Body plank line me tight rakho.", "2 second me chest floor ke paas lao.", "Poori speed se explode upar push karo (chahe haath halka lift ho)."),
            stepsEn = listOf("Brace core in a rigid plank line.", "Lower chest under control for 2 seconds.", "Explode upward at maximum punch speed."),
            mistakesHi = listOf("Elbows 90° bahar phailana (shoulder injury risk)."),
            mistakesEn = listOf("Flaring elbows out 90° wide."),
            boxingBenefitHi = "Jab aur Cross me aage ki explosive snapping power deta hai.",
            boxingBenefitEn = "Directly boosts straight punch snap and triceps lockout speed.",
            tempoCue = "2s Lower • 0.5s Explosive Drive",
            restSeconds = 60,
            baseSets = 4,
            baseReps = "15-20 reps",
            equipment = Equipment.NONE
        ),
        BoxingExercise(
            id = "pullups",
            category = ExerciseCategory.UPPER_POWER,
            motionType = ExerciseMotionType.PULL_UPS,
            nameHi = "Fighter Pull-Ups",
            nameEn = "Fighter Pull-Ups",
            muscleHi = "Lats, Upper Back, Biceps, Grip",
            muscleEn = "Lats, Upper Back, Biceps, Grip",
            howToHi = "Shoulder-width grip se chest bar tak kheecho — snap recovery ke liye.",
            howToEn = "Pull chest cleanly toward bar for fast punch retraction power.",
            stepsHi = listOf("Dead hang se shoulders pack karo.", "Elbows neeche drive karte hue chin bar ke upar lao.", "Control se poora neeche aao."),
            stepsEn = listOf("Engage shoulder blades from a dead hang.", "Drive elbows down until chin clears the bar.", "Lower under full control."),
            mistakesHi = listOf("Half-reps karna ya pair hila ke swing (kipping) karna."),
            mistakesEn = listOf("Half reps or excessive body swinging."),
            boxingBenefitHi = "Punch maarke haath bijli ki tarah wapas guard pe laane ki taqat.",
            boxingBenefitEn = "Powers ultra-fast punch retraction and clinch strength.",
            tempoCue = "1s Pull • 2s Controlled Lower",
            restSeconds = 90,
            baseSets = 4,
            baseReps = "8-12 reps",
            equipment = Equipment.PULL_UP_BAR
        ),
        BoxingExercise(
            id = "med_ball",
            category = ExerciseCategory.UPPER_POWER,
            motionType = ExerciseMotionType.MED_BALL_SLAM,
            nameHi = "Medicine Ball Power Slams",
            nameEn = "Medicine Ball Power Slams",
            muscleHi = "Rotational Core, Lats, Shoulders",
            muscleEn = "Rotational Core, Lats, Shoulders",
            howToHi = "Ball sar ke upar uthao aur poori core power se floor par slam karo.",
            howToEn = "Raise ball overhead and slam violently engaging entire core.",
            stepsHi = listOf("Toes par uthte hue ball sar ke upar le jao.", "Abs tight karke poori taqat se zameen par patko.", "Bounce par squat karke ball pakdo aur repeat."),
            stepsEn = listOf("Extend tall overhead onto balls of feet.", "Crunch core hard and slam ball straight into floor.", "Squat cleanly to catch and repeat."),
            mistakesHi = listOf("Sirf haath se fekna, hips aur core use na karna."),
            mistakesEn = listOf("Slamming only with arms without hip hinge."),
            boxingBenefitHi = "Uppercut, Hook aur Body shots me knockout rotational torque.",
            boxingBenefitEn = "Builds violent rotational knockout power for hooks & body shots.",
            tempoCue = "Maximum Speed Every Rep",
            restSeconds = 60,
            baseSets = 4,
            baseReps = "12 reps",
            equipment = Equipment.MEDICINE_BALL,
            baseWeightFactor = 0.12f
        ),
        BoxingExercise(
            id = "plank",
            category = ExerciseCategory.UPPER_POWER,
            motionType = ExerciseMotionType.PLANK_TAPS,
            nameHi = "Plank + Shoulder Taps",
            nameEn = "Plank + Shoulder Taps",
            muscleHi = "Deep Core, Obliques, Stability",
            muscleEn = "Deep Core, Obliques, Stability",
            howToHi = "Body bilkul seedhi rakho, hips hilaye bina alternate shoulder tap karo.",
            howToEn = "Brace abs tight and tap opposite shoulder without hip sway.",
            stepsHi = listOf("Pair thode chauda rakho balance ke liye.", "Right hand se left shoulder tap karo, hips bilkul steady.", "Alternate side smoothly repeat karo."),
            stepsEn = listOf("Set feet slightly wider for anti-rotation base.", "Tap opposite shoulder slowly while keeping hips frozen.", "Breathe steadily through braced abs."),
            mistakesHi = listOf("Har tap ke sath hips side me ghumana."),
            mistakesEn = listOf("Twisting hips side-to-side on each tap."),
            boxingBenefitHi = "Body punches jhelne ki core armor aur punch balance deta hai.",
            boxingBenefitEn = "Forges iron core armor to absorb body shots and stabilize punches.",
            tempoCue = "Slow Controlled Taps • Zero Hip Sway",
            restSeconds = 45,
            baseSets = 3,
            baseReps = "45-60 sec",
            equipment = Equipment.NONE
        ),
        BoxingExercise(
            id = "russian_twists",
            category = ExerciseCategory.UPPER_POWER,
            motionType = ExerciseMotionType.RUSSIAN_TWISTS,
            nameHi = "Boxer Rotational Twists",
            nameEn = "Boxer Rotational Twists",
            muscleHi = "Obliques, Transverse Abs",
            muscleEn = "Obliques, Transverse Abs",
            howToHi = "45° back lean karke shoulders left-right twist karo.",
            howToEn = "Lean back 45° and rotate torso sharply side to side.",
            stepsHi = listOf("Floor par baith ke knees bend aur heels upar uthao.", "Hands ya dumbbell chest ke samne rakh ke poora torso twist karo.", "Har twist par sharp exhale karo."),
            stepsEn = listOf("Sit in V-shape with heels slightly lifted.", "Rotate shoulders fully left and right.", "Exhale sharply on every rotation."),
            mistakesHi = listOf("Sirf haath hilana jabki shoulders ek jagah rahein."),
            mistakesEn = listOf("Moving hands only without rotating ribcage."),
            boxingBenefitHi = "Hooks aur Slips me lightning-fast kamar rotation deta hai.",
            boxingBenefitEn = "Supercharges oblique snap for hooks and defensive slips.",
            tempoCue = "Crisp Left-Right Torque",
            restSeconds = 45,
            baseSets = 3,
            baseReps = "24 twists",
            equipment = Equipment.NONE
        ),

        // SECTION 4: LEGS, BASE & INJURY PROOFING
        BoxingExercise(
            id = "squats",
            category = ExerciseCategory.LEGS_BULLETPROOF,
            motionType = ExerciseMotionType.SQUATS,
            nameHi = "Goblet / Jump Squats",
            nameEn = "Goblet / Jump Squats",
            muscleHi = "Quads, Glutes, Ground Power",
            muscleEn = "Quads, Glutes, Ground Power",
            howToHi = "Chest up, knees toes ki line me, neeche se explosive upar aao.",
            howToEn = "Sit deep with upright chest, drive explosively through mid-foot.",
            stepsHi = listOf("Dumbbell chest ke paas pakdo ya bodyweight stance lo.", "Hips peeche aur neeche parallel tak baitho.", "Heels aur balls of feet se zameen push karke explode upar."),
            stepsEn = listOf("Hold dumbbell tight at chest or use bodyweight.", "Descend until thighs reach parallel with chest tall.", "Drive powerfully upward through the floor."),
            mistakesHi = listOf("Ghutne andar ki taraf collapse hona.", "Lower back gol karna."),
            mistakesEn = listOf("Knees caving inward.", "Rounding the lower spine at the bottom."),
            boxingBenefitHi = "Bob & weave stamina aur uppercut/cross ka ground power base.",
            boxingBenefitEn = "Fuels bob-and-weave endurance and ground-up punching power.",
            tempoCue = "2s Down • 1s Explosive Drive",
            restSeconds = 75,
            baseSets = 4,
            baseReps = "15 reps",
            equipment = Equipment.DUMBBELL,
            baseWeightFactor = 0.25f
        ),
        BoxingExercise(
            id = "lunges",
            category = ExerciseCategory.LEGS_BULLETPROOF,
            motionType = ExerciseMotionType.LUNGES,
            nameHi = "Split Boxing Lunges",
            nameEn = "Split Boxing Lunges",
            muscleHi = "Quads, Glutes, Stance Stability",
            muscleEn = "Quads, Glutes, Stance Stability",
            howToHi = "Boxing stance jaisi stride me neeche drop karo, back knee floor ke paas.",
            howToEn = "Drop into split-stance lunge keeping torso upright, drive back up.",
            stepsHi = listOf("Ek pair aage rakho, back heel uthi rahe.", "Back knee floor se 1 inch upar tak neeche jao.", "Front heel push karke stance me wapas aao."),
            stepsEn = listOf("Take a long split stride with rear heel lifted.", "Lower rear knee cleanly toward the floor.", "Drive up through the front leg."),
            mistakesHi = listOf("Aage jhukna ya front knee bahut aage nikalna."),
            mistakesEn = listOf("Leaning torso forward over the front thigh."),
            boxingBenefitHi = "Har jab-cross step-in ke liye pairon me balance aur spring deta hai.",
            boxingBenefitEn = "Strengthens single-leg drive for stepping in with jabs.",
            tempoCue = "Controlled Drop • Snap Up",
            restSeconds = 60,
            baseSets = 3,
            baseReps = "12/leg",
            equipment = Equipment.DUMBBELL,
            baseWeightFactor = 0.20f
        ),
        BoxingExercise(
            id = "deadlift",
            category = ExerciseCategory.LEGS_BULLETPROOF,
            motionType = ExerciseMotionType.DEADLIFT,
            nameHi = "Dumbbell Romanian Deadlift",
            nameEn = "Dumbbell Romanian Deadlift",
            muscleHi = "Hamstrings, Glutes, Lower Back",
            muscleEn = "Hamstrings, Glutes, Lower Back",
            howToHi = "Back flat rakho, hips peeche push karo jab dumbbells shins ke paas aayein.",
            howToEn = "Hinge hips back with flat spine, drive hips forward powerfully.",
            stepsHi = listOf("Knees halka bend lock rakho, chest proud.", "Hips ko peeche deewar ki taraf push karo jab tak hamstring stretch ho.", "Glutes squeeze karke hips aage snap karo."),
            stepsEn = listOf("Keep soft knees and shoulders pulled back.", "Push hips straight back until hamstrings stretch deeply.", "Snap hips forward forcefully to lockout."),
            mistakesHi = listOf("Back gol (round) karke weights neeche latkana."),
            mistakesEn = listOf("Rounding the lower back instead of hinging hips."),
            boxingBenefitHi = "Posterior chain power — Cross aur Hook me hip snap double karta hai.",
            boxingBenefitEn = "Builds posterior chain hip snap for heavier crosses and hooks.",
            tempoCue = "3s Hinge Back • 1s Hip Snap",
            restSeconds = 90,
            baseSets = 4,
            baseReps = "10 reps",
            equipment = Equipment.DUMBBELL,
            baseWeightFactor = 0.35f
        ),
        BoxingExercise(
            id = "neck",
            category = ExerciseCategory.LEGS_BULLETPROOF,
            motionType = ExerciseMotionType.NECK_ISOMETRIC,
            nameHi = "Boxer Neck Isometric Holds",
            nameEn = "Boxer Neck Isometric Holds",
            muscleHi = "Neck Flexors, Traps, Chin Stability",
            muscleEn = "Neck Flexors, Traps, Chin Stability",
            howToHi = "Haath se forehead/temple par halka pressure do aur neck se resist karo.",
            howToEn = "Press gently against forehead/temple with palm while neck resists.",
            stepsHi = listOf("Chin neeche tuck rakho.", "Front, left side, right side aur back — har direction me 15 sec hold.", "Jhatka bilkul mat do, smooth steady pressure rakho."),
            stepsEn = listOf("Tuck chin slightly inward.", "Resist palm pressure front, left, right, and rear for 15s each.", "Use smooth isometric tension—never jerk the neck."),
            mistakesHi = listOf("Bahut tez pressure laga ke neck twist karna."),
            mistakesEn = listOf("Using jerky or excessive hand force."),
            boxingBenefitHi = "Iron Chin — punch lagne par head snap aur concussion risk kam karta hai.",
            boxingBenefitEn = "Iron Chin builder — reduces head snap on impact.",
            tempoCue = "4 Directions × 15s Steady Hold",
            restSeconds = 30,
            baseSets = 3,
            baseReps = "4×15 sec",
            equipment = Equipment.NONE
        ),
        BoxingExercise(
            id = "wrist",
            category = ExerciseCategory.LEGS_BULLETPROOF,
            motionType = ExerciseMotionType.WRIST_CURLS,
            nameHi = "Wrist Curls & Knuckle Strength",
            nameEn = "Wrist Curls & Knuckle Strength",
            muscleHi = "Forearms, Wrists, Knuckles",
            muscleEn = "Forearms, Wrists, Knuckles",
            howToHi = "Bench ya ghutne par forearm tikao, light dumbbell se wrist curl karo.",
            howToEn = "Rest forearm on thigh or bench, curl light dumbbell strictly with wrist.",
            stepsHi = listOf("Forearm flat tikao, sirf wrist move ho.", "Dumbbell neeche roll hone do, phir tight grip se upar curl karo.", "Isi tarah reverse wrist curl bhi maaro."),
            stepsEn = listOf("Anchor forearm flat so only the wrist joint moves.", "Lower fingers smoothly, then squeeze grip and curl up.", "Perform both palm-up and palm-down reps."),
            mistakesHi = listOf("Bahut heavy weight utha ke elbow hila dena."),
            mistakesEn = listOf("Going too heavy and lifting the forearm off the bench."),
            boxingBenefitHi = "Heavy bag aur sparring me wrist mudne (sprain) se bachata hai.",
            boxingBenefitEn = "Locks your wrist rigid on heavy impacts so knuckles never buckle.",
            tempoCue = "Controlled Full Range • High Reps",
            restSeconds = 45,
            baseSets = 3,
            baseReps = "18 reps",
            equipment = Equipment.BENCH,
            baseWeightFactor = 0.10f
        )
    )

    val indianFoods: List<IndianFoodItem> = listOf(
        IndianFoodItem("Uble Anda (Whole Eggs)", "Boiled Eggs", "3 Ande", "3 Whole Eggs", 210, 18),
        IndianFoodItem("Paneer Bhurji / Raw Paneer", "Fresh Paneer", "100g Paneer", "100g Paneer", 265, 18),
        IndianFoodItem("Soya Chunks (Nutrela)", "Soya Chunks", "50g Dry Soya", "50g Dry Soya", 172, 26),
        IndianFoodItem("Moong / Masoor Dal", "Lentils (Dal)", "1 Badi Katori", "1 Large Bowl", 180, 11),
        IndianFoodItem("Chawal (Steamed Rice)", "Steamed Rice", "150g Cooked", "150g Cooked", 195, 4),
        IndianFoodItem("Gehu Ki Roti", "Whole Wheat Roti", "2 Roti", "2 Rotis", 210, 7),
        IndianFoodItem("Kela (Banana) Pre-Workout", "Banana", "2 Kele", "2 Bananas", 210, 2)
    )

    fun calculateExercisePlan(exercise: BoxingExercise, weightKg: Float, goal: TrainingGoal): Pair<String, String> {
        val sets = when (goal) {
            TrainingGoal.STAMINA -> exercise.baseSets + 1
            TrainingGoal.STRENGTH -> exercise.baseSets
            TrainingGoal.FAT_LOSS -> exercise.baseSets + 1
            TrainingGoal.SKILL -> exercise.baseSets
        }
        val repsText = when {
            exercise.baseReps.contains("min") || exercise.baseReps.contains("sec") -> exercise.baseReps
            goal == TrainingGoal.STRENGTH -> "8-10 reps"
            goal == TrainingGoal.FAT_LOSS -> "18-20 reps"
            goal == TrainingGoal.STAMINA -> "15-18 reps"
            else -> exercise.baseReps
        }
        val loadKg = if (exercise.baseWeightFactor > 0f) {
            val multiplier = when (goal) {
                TrainingGoal.STRENGTH -> 1.25f
                TrainingGoal.FAT_LOSS -> 0.85f
                TrainingGoal.STAMINA -> 0.90f
                TrainingGoal.SKILL -> 0.80f
            }
            val computed = (weightKg * exercise.baseWeightFactor * multiplier).toInt().coerceAtLeast(2)
            " • ${computed} kg"
        } else ""
        return "${sets} sets × $repsText$loadKg" to repsText
    }

    fun calculateNutrition(weightKg: Float, goal: TrainingGoal): Pair<Int, Int> {
        val baseCalories = (weightKg * 32f).toInt()
        val calories = when (goal) {
            TrainingGoal.FAT_LOSS -> baseCalories - 350
            TrainingGoal.STRENGTH -> baseCalories + 300
            TrainingGoal.STAMINA -> baseCalories + 150
            TrainingGoal.SKILL -> baseCalories
        }.coerceIn(1300, 3800)

        val proteinFactor = when (goal) {
            TrainingGoal.STRENGTH -> 1.9f
            TrainingGoal.FAT_LOSS -> 1.8f
            TrainingGoal.STAMINA -> 1.6f
            TrainingGoal.SKILL -> 1.5f
        }
        val proteinG = (weightKg * proteinFactor).toInt().coerceAtLeast(60)
        return calories to proteinG
    }

    fun defaultScheduleFor(plan: SchedulePlanType): List<Triple<Int, String, Pair<Boolean, List<ScheduledExercise>>>> {
        fun ex(name: String, sets: Int, reps: String, eq: String) =
            ScheduledExercise(UUID.randomUUID().toString(), name, sets, reps, eq)

        return when (plan) {
            SchedulePlanType.BEGINNER, SchedulePlanType.CUSTOM -> listOf(
                Triple(1, "Boxing Fundamentals (Mon)", false to listOf(
                    ex("Boxing Jump Rope (Skipping)", 3, "2 min", "Skipping Rope"),
                    ex("Shadow Boxing Flow (1-2 Focus)", 4, "3 min", "Bodyweight"),
                    ex("Boxer Neck Isometric Holds", 3, "15 sec", "Bodyweight")
                )),
                Triple(2, "Gym Strength Base (Tue)", false to listOf(
                    ex("Explosive Boxing Push-Ups", 4, "12 reps", "Bodyweight"),
                    ex("Goblet / Jump Squats", 4, "12 reps", "Dumbbell"),
                    ex("Plank + Shoulder Taps", 3, "45 sec", "Bodyweight")
                )),
                Triple(3, "Boxing Combos & Footwork (Wed)", false to listOf(
                    ex("Shadow Ring Footwork Drill", 3, "3 min", "Bodyweight"),
                    ex("Heavy Bag Power Rounds", 4, "3 min", "Heavy Bag"),
                    ex("Wrist Curls & Knuckle Strength", 3, "15 reps", "Dumbbell")
                )),
                Triple(4, "Gym Power & Legs (Thu)", false to listOf(
                    ex("Split Boxing Lunges", 3, "12/leg", "Dumbbell"),
                    ex("Dumbbell Romanian Deadlift", 4, "10 reps", "Dumbbell"),
                    ex("Fighter Pull-Ups", 3, "8 reps", "Pull-up Bar")
                )),
                Triple(5, "Boxing Sparring Flow (Fri)", false to listOf(
                    ex("Boxing Jump Rope (Skipping)", 4, "3 min", "Skipping Rope"),
                    ex("Reflex Slip & Counter Drill", 5, "3 min", "Bodyweight"),
                    ex("Medicine Ball Power Slams", 3, "12 reps", "Medicine Ball")
                )),
                Triple(6, "Gym Conditioning (Sat)", false to listOf(
                    ex("Roadwork / HIIT Sprints", 1, "20 min", "Bodyweight"),
                    ex("Explosive Fight Burpees", 4, "12 reps", "Bodyweight"),
                    ex("Fast Mountain Climbers", 3, "40 sec", "Bodyweight")
                )),
                Triple(7, "Complete Rest Day (Sun)", true to emptyList())
            )
            SchedulePlanType.INTERMEDIATE -> listOf(
                Triple(1, "Speed Boxing + Defense (Mon)", false to listOf(
                    ex("Boxing Jump Rope (Skipping)", 5, "3 min", "Skipping Rope"),
                    ex("Shadow Boxing Flow + Slip/Parry", 6, "3 min", "Bodyweight"),
                    ex("Boxer Neck Isometric Holds", 4, "20 sec", "Bodyweight")
                )),
                Triple(2, "Explosive Strength (Tue)", false to listOf(
                    ex("Explosive Boxing Push-Ups", 5, "18 reps", "Bodyweight"),
                    ex("Fighter Pull-Ups", 4, "10 reps", "Pull-up Bar"),
                    ex("Goblet / Jump Squats", 4, "15 reps", "Dumbbell")
                )),
                Triple(3, "Heavy Bag Power (Wed)", false to listOf(
                    ex("Heavy Bag Power Rounds", 6, "3 min", "Heavy Bag"),
                    ex("Medicine Ball Power Slams", 4, "15 reps", "Medicine Ball")
                )),
                Triple(4, "Footwork & Angles (Thu)", false to listOf(
                    ex("Shadow Ring Footwork Drill", 4, "3 min", "Bodyweight"),
                    ex("Reflex Slip & Counter Drill", 5, "3 min", "Bodyweight"),
                    ex("Plank + Shoulder Taps", 4, "60 sec", "Bodyweight")
                )),
                Triple(5, "Gym Hypertrophy & Core (Fri)", false to listOf(
                    ex("Dumbbell Romanian Deadlift", 4, "12 reps", "Dumbbell"),
                    ex("Split Boxing Lunges", 4, "12/leg", "Dumbbell"),
                    ex("Wrist Curls & Knuckle Strength", 4, "18 reps", "Dumbbell")
                )),
                Triple(6, "Championship Rounds (Sat)", false to listOf(
                    ex("Roadwork / HIIT Sprints", 1, "25 min", "Bodyweight"),
                    ex("Shadow Boxing Flow", 8, "3 min", "Bodyweight")
                )),
                Triple(7, "Active Recovery (Sun)", false to listOf(
                    ex("Speed Bag / Air Rhythm", 2, "5 min", "Bodyweight")
                ))
            )
        }
    }

    val badges: List<HunterBadge> = listOf(
        HunterBadge("first_blood", "First Bell", "First Bell", "Pehla live session complete karo", "Complete your first live session", false),
        HunterBadge("jab_master", "Jab Sniper", "Jab Sniper", "Ek session me 50+ punches maaro", "Land 50+ punches in a single session", false),
        HunterBadge("iron_guard", "Iron Guard", "Iron Guard", "85%+ guard discipline score pao", "Achieve 85%+ guard discipline", false),
        HunterBadge("streak_7", "7-Day Champ", "7-Day Champ", "7 din lagatar training streak", "Maintain a 7-day training streak", false),
        HunterBadge("s_rank", "World Champion", "World Champion", "Level 22 (S-Rank) tak pahuncho", "Reach Level 22 (S-Rank)", false)
    )
}
