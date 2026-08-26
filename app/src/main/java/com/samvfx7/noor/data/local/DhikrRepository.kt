package com.samvfx7.noor.data.local

import com.samvfx7.noor.data.model.DhikrCategory
import com.samvfx7.noor.data.model.DhikrItem

object DhikrRepository {

    val PRESET_ADHKAR = listOf(
        // Morning Adhkar
        DhikrItem(
            id = 101,
            arabic = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "Asbahnaa wa asbahal-mulku lillaahi wal-hamdu lillaahi, laa ilaaha illal-laahu wahdahu laa shareeka lahu, lahul-mulku wa lahul-hamdu wa Huwa 'alaa kulli shay'in Qadeer.",
            translation = "We have entered the morning and the kingdom belongs to Allah, praise be to Allah. None has the right to be worshipped except Allah alone, without partner. To Him belongs the dominion and praise, and He has power over all things.",
            targetCount = 1,
            category = DhikrCategory.MORNING,
            reference = "Sahih Muslim 2723",
            benefit = "A comprehensive declaration of tawhid, gratitude, and refuge at dawn."
        ),
        DhikrItem(
            id = 102,
            arabic = "اللَّهُمَّ بِكَ أَصْبَحْنَا، وَبِكَ أَمْسَيْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ النُّشُورُ",
            transliteration = "Allaahumma bika asbahnaa, wa bika amsaynaa, wa bika nahyaa, wa bika namootu wa ilaykan-nushoor.",
            translation = "O Allah, by Your leave we have reached the morning and by Your leave we have reached the evening, by Your leave we live and die, and unto You is our resurrection.",
            targetCount = 1,
            category = DhikrCategory.MORNING,
            reference = "At-Tirmidhi 3391 (Sahih)",
            benefit = "Affirms reliance on Allah for life, death, and resurrection."
        ),
        DhikrItem(
            id = 103,
            arabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ: عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ",
            transliteration = "Subhaan-Allaahi wa bihamdihi: 'Adada khalqihi, wa ridaa nafsihi, wa zinata 'arshihi, wa midaada kalimaatih.",
            translation = "Glory is to Allah and praise is to Him, by the number of His creation and His pleasure, and by the weight of His throne, and the ink of His words.",
            targetCount = 3,
            category = DhikrCategory.MORNING,
            reference = "Sahih Muslim 2726",
            benefit = "Prophet ﷺ said this outweighs hours of constant tasbih."
        ),
        DhikrItem(
            id = 104,
            arabic = "اللَّهُمَّ عَافِنِي فِي بَدَنِي، اللَّهُمَّ عَافِنِي فِي سَمْعِي، اللَّهُمَّ عَافِنِي فِي بَصَرِي، لَا إِلَٰهَ إِلَّا أَنْتَ",
            transliteration = "Allaahumma 'aafinee fee badanee, Allaahumma 'aafinee fee sam'ee, Allaahumma 'aafinee fee basaree, laa ilaaha illaa Anta.",
            translation = "O Allah, grant me health in my body. O Allah, grant me health in my hearing. O Allah, grant me health in my sight. There is no deity worthy of worship except You.",
            targetCount = 3,
            category = DhikrCategory.MORNING,
            reference = "Abu Dawud 5090",
            benefit = "Daily supplication for physical and spiritual well-being."
        ),
        DhikrItem(
            id = 105,
            arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillaahil-lazee laa yadurru ma'as-mihi shay'un fil-ardi wa laa fis-samaaa'i wa Huwas-Samee'ul-'Aleem.",
            translation = "In the Name of Allah, with whose Name nothing can cause harm in the earth nor in the heavens, and He is the All-Hearing, the All-Knowing.",
            targetCount = 3,
            category = DhikrCategory.MORNING,
            reference = "Abu Dawud & At-Tirmidhi",
            benefit = "Whoever says this 3 times morning & evening will be protected from all harm."
        ),

        // Evening Adhkar
        DhikrItem(
            id = 201,
            arabic = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "Amsaynaa wa amsal-mulku lillaahi wal-hamdu lillaahi, laa ilaaha illal-laahu wahdahu laa shareeka lahu, lahul-mulku wa lahul-hamdu wa Huwa 'alaa kulli shay'in Qadeer.",
            translation = "We have reached the evening and the dominion belongs to Allah, praise is to Allah. There is no true deity except Allah alone without partner.",
            targetCount = 1,
            category = DhikrCategory.EVENING,
            reference = "Sahih Muslim 2723",
            benefit = "Evening remembrance invoking Allah's dominion and protection."
        ),
        DhikrItem(
            id = 202,
            arabic = "اللَّهُمَّ بِكَ أَمْسَيْنَا، وَبِكَ أَصْبَحْنَا، وَبِكَ نَحْيَا، وَبِكَ نَمُوتُ، وَإِلَيْكَ الْمَصِيرُ",
            transliteration = "Allaahumma bika amsaynaa, wa bika asbahnaa, wa bika nahyaa, wa bika namootu wa ilaykal-maseer.",
            translation = "O Allah, by Your leave we have reached the evening and by Your leave we have reached the morning, by You we live and die, and unto You is the return.",
            targetCount = 1,
            category = DhikrCategory.EVENING,
            reference = "At-Tirmidhi 3391",
            benefit = "Peace of mind and submission during the sunset hours."
        ),
        DhikrItem(
            id = 203,
            arabic = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            transliteration = "A'oozu bi kalimaatil-laahit-taammaati min sharri maa khalaq.",
            translation = "I seek refuge in the Perfect Words of Allah from the evil of what He has created.",
            targetCount = 3,
            category = DhikrCategory.EVENING,
            reference = "Sahih Muslim 2709",
            benefit = "Protection from poisonous stings, illnesses, and harm through the night."
        ),

        // After Prayer Adhkar
        DhikrItem(
            id = 301,
            arabic = "أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ. اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
            transliteration = "Astaghfirullaah (3x). Allaahumma Antas-Salaamu wa minkas-salaam, tabaarakta yaa Zal-Jalaali wal-Ikraam.",
            translation = "I ask Allah for forgiveness (3 times). O Allah, You are Peace and from You comes peace. Blessed are You, O Owner of majesty and honor.",
            targetCount = 1,
            category = DhikrCategory.AFTER_PRAYER,
            reference = "Sahih Muslim 591",
            benefit = "Sunnah immediately following the tasleem of obligatory prayer."
        ),
        DhikrItem(
            id = 302,
            arabic = "سُبْحَانَ اللَّهِ",
            transliteration = "Subhaan-Allaah",
            translation = "Glory be to Allah.",
            targetCount = 33,
            category = DhikrCategory.AFTER_PRAYER,
            reference = "Sahih Muslim 597",
            benefit = "Part of the 33-33-33 post-prayer tasbih that forgives sins."
        ),
        DhikrItem(
            id = 303,
            arabic = "الْحَمْدُ لِلَّهِ",
            transliteration = "Alhamdu lillaah",
            translation = "All praise is due to Allah.",
            targetCount = 33,
            category = DhikrCategory.AFTER_PRAYER,
            reference = "Sahih Muslim 597",
            benefit = "Fills the scale of good deeds with immense reward."
        ),
        DhikrItem(
            id = 304,
            arabic = "اللَّهُ أَكْبَرُ",
            transliteration = "Allaahu Akbar",
            translation = "Allah is the Greatest.",
            targetCount = 33,
            category = DhikrCategory.AFTER_PRAYER,
            reference = "Sahih Muslim 597",
            benefit = "Magnifying Allah completes the 99 counts before closing with Tawhid."
        ),
        DhikrItem(
            id = 305,
            arabic = "لَا إِلَٰهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "Laa ilaaha illal-laahu wahdahu laa shareeka lahu, lahul-mulku wa lahul-hamdu wa Huwa 'alaa kulli shay'in Qadeer.",
            translation = "None has the right to be worshipped except Allah alone, without partner. To Him belongs all praise and dominion, and He is over all things competent.",
            targetCount = 1,
            category = DhikrCategory.AFTER_PRAYER,
            reference = "Sahih Muslim 597",
            benefit = "Seals the 100th count of post-prayer adhkar."
        ),

        // General Dhikr
        DhikrItem(
            id = 401,
            arabic = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
            transliteration = "Subhaan-Allaahi wa bihamdihi, Subhaan-Allaahil-'Azeem.",
            translation = "Glory be to Allah and His is the praise, glory be to Allah the Supreme.",
            targetCount = 100,
            category = DhikrCategory.GENERAL,
            reference = "Sahih al-Bukhari 6406",
            benefit = "Two phrases beloved to the Most Merciful, light on the tongue, heavy on the Scale."
        ),
        DhikrItem(
            id = 402,
            arabic = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            transliteration = "Laa hawla wa laa quwwata illaa billaah.",
            translation = "There is no power and no strength except with Allah.",
            targetCount = 100,
            category = DhikrCategory.GENERAL,
            reference = "Sahih al-Bukhari 6384",
            benefit = "One of the treasures of Paradise."
        ),
        DhikrItem(
            id = 403,
            arabic = "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ",
            transliteration = "Astaghfirullaaha wa atoobu ilayh.",
            translation = "I seek Allah's forgiveness and repent to Him.",
            targetCount = 100,
            category = DhikrCategory.GENERAL,
            reference = "Sahih al-Bukhari 6307",
            benefit = "The Prophet ﷺ sought forgiveness more than 70 to 100 times daily."
        ),

        // Sleep Adhkar
        DhikrItem(
            id = 501,
            arabic = "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي وَبِكَ أَرْفَعُهُ، إِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ",
            transliteration = "Bismika Rabbee wada'tu jambee wa bika arfa'uh, in amsakta nafsee farhamhaa, wa in arsaltahaa fahfazhaa bimaa tahfazu bihi 'ibaadakas-saaliheen.",
            translation = "In Your name my Lord, I lie down and in Your name I arise. If You should take my soul, have mercy upon it, and if You should return my soul, protect it as You protect Your righteous slaves.",
            targetCount = 1,
            category = DhikrCategory.SLEEP,
            reference = "Sahih al-Bukhari 6320",
            benefit = "Sunnah protection recited upon getting into bed."
        ),
        DhikrItem(
            id = 502,
            arabic = "اللَّهُمَّ قِنِي عَذَابَكَ يَوْمَ تَبْعَثُ عِبَادَكَ",
            transliteration = "Allaahumma qinee 'azaabaka yawma tab'asu 'ibaadak.",
            translation = "O Allah, protect me from Your punishment on the Day You resurrect Your servants.",
            targetCount = 3,
            category = DhikrCategory.SLEEP,
            reference = "Abu Dawud 5045",
            benefit = "Recited with right hand placed under the right cheek."
        )
    )
}
