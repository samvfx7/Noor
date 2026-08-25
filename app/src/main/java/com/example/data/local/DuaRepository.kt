package com.example.data.local

import com.example.data.model.DuaCategory
import com.example.data.model.DuaItem

object DuaRepository {

    val DUAS_LIST = listOf(
        // Morning & Evening
        DuaItem(
            id = "dua_sayyidul_istighfar",
            title = "Sayyid al-Istighfar (Chief of Forgiveness)",
            category = DuaCategory.FORGIVENESS,
            arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَٰهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَىٰ عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ لَكَ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            transliteration = "Allaahumma Anta Rabbee laa ilaaha illaa Anta, khalaqtanee wa ana 'abduka, wa ana 'alaa 'ahdika wa wa'dika mas-tata'tu, a'oozu bika min sharri maa sana'tu, aboo'u laka bini'matika 'alayya, wa aboo'u laka bizambee faghfir lee fa-innahu laa yaghfiruz-zunooba illaa Anta.",
            translation = "O Allah, You are my Lord, there is no god but You. You created me and I am Your servant, and I abide by Your covenant and promise as best I can. I seek refuge in You from the evil of what I have done. I acknowledge Your favors upon me and I acknowledge my sin, so forgive me, for none forgives sins except You.",
            reference = "Sahih al-Bukhari 6306",
            whenToRead = "Read in the morning and evening with firm conviction."
        ),

        // Sleep & Waking
        DuaItem(
            id = "dua_waking_up",
            title = "Dua Upon Waking Up",
            category = DuaCategory.SLEEP_WAKE,
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
            transliteration = "Alhamdu lillaahil-lazee ahyaanaa ba'da maa amaatanaa wa ilayhin-nushoor.",
            translation = "All praise is for Allah who gave us life after having taken it from us, and unto Him is the resurrection.",
            reference = "Sahih al-Bukhari 6312",
            whenToRead = "Immediately upon opening your eyes in the morning."
        ),
        DuaItem(
            id = "dua_before_sleep",
            title = "Dua Before Sleeping",
            category = DuaCategory.SLEEP_WAKE,
            arabic = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            transliteration = "Bismik-Allaahumma amootu wa ahyaa.",
            translation = "In Your Name, O Allah, I die and I live.",
            reference = "Sahih al-Bukhari 6324",
            whenToRead = "When resting head on pillow before falling asleep."
        ),

        // Prayer & Mosque
        DuaItem(
            id = "dua_entering_mosque",
            title = "Entering the Mosque",
            category = DuaCategory.PRAYER_MOSQUE,
            arabic = "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
            transliteration = "Allaahum-maftah lee abwaaba rahmatik.",
            translation = "O Allah, open for me the gates of Your mercy.",
            reference = "Sahih Muslim 713",
            whenToRead = "Step in with right foot upon entering mosque."
        ),
        DuaItem(
            id = "dua_leaving_mosque",
            title = "Leaving the Mosque",
            category = DuaCategory.PRAYER_MOSQUE,
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
            transliteration = "Allaahumma innee as-aluka min fadlik.",
            translation = "O Allah, I ask You from Your bounty.",
            reference = "Sahih Muslim 713",
            whenToRead = "Step out with left foot upon leaving mosque."
        ),
        DuaItem(
            id = "dua_qiyam_night",
            title = "Dua for Tahajjud / Qiyam",
            category = DuaCategory.PRAYER_MOSQUE,
            arabic = "اللَّهُمَّ لَكَ الْحَمْدُ أَنْتَ نُورُ السَّمَاوَاتِ وَالْأَرْضِ وَمَنْ فِيهِنَّ، وَلَكَ الْحَمْدُ أَنْتَ قَيِّمُ السَّمَاوَاتِ وَالْأَرْضِ وَمَنْ فِيهِنَّ",
            transliteration = "Allaahumma lakal-hamdu Anta noorus-samaawaati wal-ardi wa man feehinna, wa lakal-hamdu Anta qayyimus-samaawaati wal-ardi wa man feehinna...",
            translation = "O Allah, to You belongs all praise. You are the Light of the heavens and the earth and all that is within them. To You belongs all praise, You are the Sustainer of the heavens and earth...",
            reference = "Sahih al-Bukhari 1120",
            whenToRead = "Opening supplication for Night Prayer (Tahajjud)."
        ),

        // Travel & Journey
        DuaItem(
            id = "dua_travel_vehicle",
            title = "Dua for Riding a Vehicle / Traveling",
            category = DuaCategory.TRAVEL,
            arabic = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَٰذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ، وَإِنَّا إِلَىٰ رَبِّنَا لَمُنْقَلِبُونَ",
            transliteration = "Subhaanal-lazee sakh-khara lanaa haazaa wa maa kunnaa lahu muqrineen, wa innaaa ilaa Rabbinaa lamunqaliboon.",
            translation = "Glory to Him who has subjected this to us, though we were not capable of handling it by ourselves, and indeed to our Lord we will return.",
            reference = "Surah Az-Zukhruf (43:13-14) / Sahih Muslim 1342",
            whenToRead = "Upon mounting a vehicle, car, plane, or train."
        ),

        // Protection
        DuaItem(
            id = "dua_anxiety_grief",
            title = "Relief from Anxiety and Worry",
            category = DuaCategory.PROTECTION,
            arabic = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْبُخْلِ وَالْجُبْنِ، وَضَلَعِ الدَّيْنِ، وَغَلَبَةِ الرِّجَالِ",
            transliteration = "Allaahumma innee a'oozu bika minal-hammi wal-hazan, wal-'ajzi wal-kasal, wal-bukhli wal-jubn, wa dala'id-dayni wa ghalabatir-rijaal.",
            translation = "O Allah, I seek refuge in You from grief and sadness, from weakness and laziness, from miserliness and cowardice, from being heavily in debt and being overpowered by men.",
            reference = "Sahih al-Bukhari 2893",
            whenToRead = "When feeling overwhelmed, anxious, or facing debt."
        ),
        DuaItem(
            id = "dua_leaving_house",
            title = "Leaving the House",
            category = DuaCategory.PROTECTION,
            arabic = "بِسْمِ اللَّهِ، تَوَكَّلْتُ عَلَى اللَّهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            transliteration = "Bismillaahi, tawakkaltu 'alal-laahi, wa laa hawla wa laa quwwata illaa billaah.",
            translation = "In the Name of Allah, I place my trust in Allah, and there is no might nor power except with Allah.",
            reference = "Abu Dawud 5095 (Sahih)",
            whenToRead = "Whenever stepping outside the home."
        ),

        // Food & Drink
        DuaItem(
            id = "dua_before_eating",
            title = "Before Eating",
            category = DuaCategory.FOOD_DRINK,
            arabic = "بِسْمِ اللَّهِ (فَإِنْ نَسِيَ: بِسْمِ اللَّهِ فِي أَوَّلِهِ وَآخِرِهِ)",
            transliteration = "Bismillaah (If forgotten: Bismillaahi fee awwalihi wa aakhirihi)",
            translation = "In the Name of Allah (If forgotten at start: In the Name of Allah at its beginning and end).",
            reference = "Abu Dawud 3767",
            whenToRead = "Before taking the first bite or sip."
        ),
        DuaItem(
            id = "dua_after_eating",
            title = "After Finishing Meal",
            category = DuaCategory.FOOD_DRINK,
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَٰذَا وَرَزَقَنِيهِ مِنْ غَيْرِ حَوْلٍ مِنِّي وَلَا قُوَّةٍ",
            transliteration = "Alhamdu lillaahil-lazee at'amanee haazaa wa razaqaneehi min ghayri hawlin minnee wa laa quwwah.",
            translation = "All praise is to Allah who gave me this food and provided it for me with no power or might on my part.",
            reference = "At-Tirmidhi 3458 (Hasan)",
            whenToRead = "Upon finishing food or drink."
        ),

        // Fasting & Ramadan
        DuaItem(
            id = "dua_breaking_fast",
            title = "Breaking the Fast (Iftar)",
            category = DuaCategory.RAMADAN_FASTING,
            arabic = "ذَهَبَ الظَّمَأُ وَابْتَلَّتِ الْعُرُوقُ، وَثَبَتَ الْأَجْرُ إِنْ شَاءَ اللَّهُ",
            transliteration = "Zahabaz-zama'u wabtallatil-'urooqu wa thabatal-ajru in shaa'Allaah.",
            translation = "The thirst has gone, the veins are moistened, and the reward is confirmed, if Allah wills.",
            reference = "Sunan Abi Dawud 2357 (Hasan)",
            whenToRead = "Immediately upon breaking fast at Maghrib."
        ),
        DuaItem(
            id = "dua_laylatul_qadr",
            title = "Dua for Laylat al-Qadr",
            category = DuaCategory.RAMADAN_FASTING,
            arabic = "اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي",
            transliteration = "Allaahumma innaka 'Afuwwun tuhibbul-'afwa fa'fu 'annee.",
            translation = "O Allah, You are Most Forgiving and You love forgiveness, so forgive me.",
            reference = "At-Tirmidhi 3513 (Sahih)",
            whenToRead = "During the last ten nights of Ramadan."
        ),

        // Istikhara
        DuaItem(
            id = "dua_istikhara",
            title = "Dua al-Istikharah (Seeking Guidance)",
            category = DuaCategory.PRAISE_THANKS,
            arabic = "اللَّهُمَّ إِنِّي أَسْتَخِيرُكَ بِعِلْمِكَ، وَأَسْتَقْدِرُكَ بِقُدْرَتِكَ، وَأَسْأَلُكَ مِنْ فَضْلِكَ الْعَظِيمِ، فَإِنَّكَ تَقْدِرُ وَلَا أَقْدِرُ، وَتَعْلَمُ وَلَا أَعْلَمُ، وَأَنْتَ عَلَّامُ الْغُيُوبِ",
            transliteration = "Allaahumma innee astakheeruka bi'ilmika, wa astaadiruka bi-qudratika, wa as-aluka min fadlikal-'azeem...",
            translation = "O Allah, I seek Your counsel through Your knowledge, and I seek strength from Your power, and I ask for Your great bounty. For You have power and I do not, and You know and I do not, and You are the Knower of the unseen.",
            reference = "Sahih al-Bukhari 1162",
            whenToRead = "After praying 2 units of non-obligatory prayer when making a decision."
        )
    )
}
