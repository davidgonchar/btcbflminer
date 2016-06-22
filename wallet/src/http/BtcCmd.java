package http;

/**
 * Created by david_000 on 22 יוני 2016.
 */

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("btccmd")
public class BtcCmd {
    @GET
    @Path("getPools")
    @Produces(MediaType.APPLICATION_JSON)
    public Result getPools(){
        Result result = new Result("get_pools");
        result.setOutput("");
        return result;
    }

    @GET
    @Path("pool")
    @Produces(MediaType.APPLICATION_JSON)
    public Result pool(@QueryParam("pool") String pool){
        Result result = new Result("pool");
        result.setInput(pool);
        result.setOutput("");
        return result;
    }

    static class Result{
        String input;
        String output;
        String action;

        public Result(){}

        public Result(String action) {
            this.action = action;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getInput() {
            return input;
        }

        public void setInput(String input) {
            this.input = input;
        }

        public String getOutput() {
            return output;
        }

        public void setOutput(String output) {
            this.output = output;
        }
    }
}
