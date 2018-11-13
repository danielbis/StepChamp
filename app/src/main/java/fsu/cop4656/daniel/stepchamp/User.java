package fsu.cop4656.daniel.stepchamp;

import java.util.HashMap;

public class User {
    public String nickname;
    public String latitude;
    public String longitude;
    public int totalsteps;

    public User(){


    }

    public User(String newname,String newlatitude,String newlongitude, int newsteps){

        nickname = newname;
        latitude = newlatitude;
        longitude = newlongitude ;
        totalsteps = newsteps;
    }


    public HashMap<String,Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("nickname",nickname);
        result.put("latitude",latitude);
        result.put("longitude",longitude);
        result.put("totalsteps",totalsteps);

        return result;

    }

}

