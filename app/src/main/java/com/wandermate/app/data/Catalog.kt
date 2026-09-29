package com.wandermate.app.data

/** Bundled editorial starter guides; costs are planning estimates, never live quotes. */
object Catalog {
    val destinations = listOf(
        Destination("nyc", "New York City", "New York, USA", "Big city. Little discoveries.",
            "Build a city break around neighborhood walks, museum visits, and generous breaks between stops.",
            "Spring and fall for comfortable walking", "Use subway and walking; check MTA service notices before departure.",
            listOf("Reserve timed-entry attractions in advance.", "Group stops by neighborhood to reduce travel.", "English is widely used. Restaurant tipping is customary; check your bill for service charges."),
            40.758, -73.9855, "https://www.nyctourism.com/", "https://images.unsplash.com/photo-1485871981521-5b1fd3805eee?w=1000&q=80"),
        Destination("vb", "Virginia Beach", "Virginia, USA", "Slow mornings by the ocean.",
            "Mix beach time with a boardwalk stroll and indoor activities when the weather changes.",
            "Late spring through early fall", "A car is useful beyond the oceanfront; check seasonal trolley service.",
            listOf("Check beach flags and local conditions before swimming.", "Bring sun protection and water.", "Leave wildlife undisturbed; use marked access paths."),
            36.8529, -75.978, "https://www.visitvirginiabeach.com/", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1000&q=80"),
        Destination("dc", "Washington, DC", "District of Columbia, USA", "A capital full of curiosity.",
            "Combine museums and monuments with relaxed neighborhood meals. Leave room for security queues.",
            "Spring and fall; popular events draw crowds", "Metrorail, Metrobus, and walking connect central sights.",
            listOf("Some free museums still require timed passes.", "The National Mall involves substantial walking.", "Use official museum pages to check closures and bag policies."),
            38.8895, -77.0353, "https://washington.org/", "https://images.unsplash.com/photo-1501466044931-62695aada8e9?w=1000&q=80"),
        Destination("bw", "Blackwater Falls", "West Virginia, USA", "Take the scenic route.",
            "A mountain getaway with waterfall overlooks, forest walks, and small-town stops near Davis and Thomas.",
            "Fall foliage and summer hiking; winter conditions vary", "A car is needed; mobile coverage can be limited.",
            listOf("Save your guide before entering areas with weak reception.", "Stay on marked trails and keep distance from wildlife.", "Check park advisories and road conditions before leaving."),
            39.108, -79.497, "https://wvstateparks.com/park/blackwater-falls-state-park/", "https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=1000&q=80"),
    )
    val places = listOf(
        Place("central", "nyc", "Central Park", "Nature", "Walk a small section of the park and plan a picnic break.", 40.7711,-73.9742,120,0,true,false,"https://www.centralparknyc.org/", "Many paved paths; route gradients vary."),
        Place("met", "nyc", "The Metropolitan Museum of Art", "Culture", "Choose a few galleries for a relaxed art visit.",40.7794,-73.9632,150,30,true,true,"https://www.metmuseum.org/", "Check official accessible entrances and admission eligibility."),
        Place("highline", "nyc", "The High Line", "Nature", "An elevated garden walk above Manhattan's west side.",40.748,-74.0048,90,0,true,false,"https://www.thehighline.org/", "Elevators at selected entrances; verify elevator status."),
        Place("natural", "nyc", "American Museum of Natural History", "Culture", "Explore natural history galleries at your own pace.",40.7813,-73.974,150,30,true,true,"https://www.amnh.org/", "Accessible entrances and elevators; check current visitor information."),
        Place("boardwalk", "vb", "Oceanfront Boardwalk", "Beach", "A flexible waterfront walk with space for a break.",36.855,-75.975,90,0,true,false,"https://www.visitvirginiabeach.com/", "Paved boardwalk; beach access varies."),
        Place("aquarium", "vb", "Virginia Aquarium", "Culture", "An indoor option for exploring marine life.",36.821,-75.983,150,35,true,true,"https://www.virginiaaquarium.com/", "Check facility access information and current ticket options."),
        Place("landing", "vb", "First Landing State Park", "Nature", "Explore a short forest trail or visit the bay shore.",36.918,-76.051,120,10,true,false,"https://www.dcr.virginia.gov/state-parks/first-landing", "Trail surfaces vary; parking fees may apply per vehicle."),
        Place("airspace", "dc", "National Air and Space Museum", "Culture", "Discover aviation and space exhibits.",38.8882,-77.0199,150,0,true,true,"https://airandspace.si.edu/", "Check timed-pass requirements and accessible facilities."),
        Place("lincoln", "dc", "Lincoln Memorial", "History", "Visit the memorial and nearby reflecting pool.",38.8893,-77.0502,60,0,true,false,"https://www.nps.gov/linc/", "Check elevator availability and current construction notices."),
        Place("history", "dc", "National Museum of Natural History", "Culture", "Plan a few favorite exhibits instead of rushing every gallery.",38.8913,-77.026,150,0,true,true,"https://naturalhistory.si.edu/", "Accessible entrance and elevators; check visitor guidance."),
        Place("falls", "bw", "Blackwater Falls Overlook", "Nature", "Enjoy the waterfall from designated viewing areas.",39.113,-79.483,75,0,true,false,"https://wvstateparks.com/park/blackwater-falls-state-park/", "Main boardwalk has many steps; check alternative viewing access."),
        Place("lindy", "bw", "Lindy Point", "Nature", "A short trail leads to a canyon overlook.",39.096,-79.519,90,0,false,false,"https://wvstateparks.com/park/blackwater-falls-state-park/", "Uneven trail and exposed overlook; not stroller friendly."),
        Place("davis", "bw", "Davis Town Walk", "Culture", "Explore local shops and take a food break.",39.129,-79.466,90,0,true,false,"https://canaanvalley.org/", "Individual shop access and hours vary."),
    )
    fun destination(id: String) = destinations.first { it.id == id }
    fun place(id: String) = places.firstOrNull { it.id == id }
    fun placesFor(id: String) = places.filter { it.destinationId == id }
}
