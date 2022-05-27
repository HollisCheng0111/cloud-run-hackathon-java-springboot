package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Random;

@SpringBootApplication
@RestController
public class Application {

    static class Self {
        public String href;
    }

    static class Links {
        public Self self;
    }

    static class PlayerState {
        public Integer x;
        public Integer y;
        public String direction;
        public Boolean wasHit;
        public Integer score;
    }

    static class Arena {
        public List<Integer> dims;
        public Map<String, PlayerState> state;
    }

    static class ArenaUpdate {
        public Links _links;
        public Arena arena;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.initDirectFieldAccess();
    }

    @GetMapping("/")
    public String index() {
        return "Let the battle begin!";
    }

    @PostMapping("/**")
    public String index(@RequestBody ArenaUpdate arenaUpdate) {
        System.out.println(arenaUpdate);
        String[] commands = new String[]{"F", "R", "L", "T"};
        Mpa<String, PlayerState> map = arenaUpdate.arena.state;
        List<PlayerState> allPlayerState = new ArrayList();
        PlayerState myState = new PlayerState();
        for (var entry : map.entrySet()) {
            String url = entry.getKey();
            PlayerState playState = entry.getValue();
            if (arenaUpdate._links.self.href.equeal(url)) {
                myState = playState;
            } else {
                allPlayerState.add(playState);
            }
        }
        int maxWallEnd = arenaUpdate.arena.dims.get(0);
        int maxWallBottom = arenaUpdate.arena.dims.get(1);

        int myX = myState.x;
        int myY = myState.y;
        String myDirection = myState.direction;

        //find ppl to attack
        for (int i = 0; i < allPlayerState.size(); i++) {
            PlayerState playerState = allPlayerState.get(i);
            if (playerState.x == myX) {
                if ((myY - playerState.y) <= 3 && >0){
                    if (myDirection.equal("W")) {
                        return "T";
                    }
                }

                if ((playerState.y - myY) <= 3 && >0){
                    if (myDirection.equal("E")) {
                        return "T";
                    }
                }
            }

            if (playerState.y == myY) {
                if ((myX - playerState.x) <= 3 && >0){
                    if (myDirection.equal("N")) {
                        return "T";
                    }
                }

                if ((playerState.x - myX) <= 3 && >0){
                    if (myDirection.equal("S")) {
                        return "T";
                    }
                }
            }
        }

        // do not facing wall to Forward
        if (myX == 0 && myDirection.equal("W")) {
            return "L";
        } else if (myY == 0 && myDirection.equal("N")) {
            return "L"
        } else if (myX == maxWallEnd && myDirection.equal("E")) {
            return "R";
        } else if (myY == maxWallBottom && myDirection.equal("S")) {
            return "R";
        }

        return "F";


        // int i = new Random().nextInt(4);
//        return commands[i];
    }

}

