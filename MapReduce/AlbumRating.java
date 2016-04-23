package Music;

 /**
 * The Goal of this program is to find the average rating of an album based on the avg rating of the songs in it.
 */

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;
import java.util.HashMap;

public class AlbumRating extends Configured implements Tool {

    public static void execute(String[] args){
        int res = 0;
        try {
            res = ToolRunner.run(new AlbumRating(), args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(res);
    }


  public int run(String[] args) throws Exception {
    Job job = Job.getInstance(getConf(), "AlbumRating");
    job.setJarByClass(this.getClass());


  // Reading Input and Output file from the command line arguments
    FileInputFormat.addInputPath(job, new Path(args[1]));
    FileOutputFormat.setOutputPath(job, new Path(args[2]));

  // Setting up Mapper and Reducer
    job.setMapperClass(Map.class);
    job.setReducerClass(Reduce.class);
    job.setNumReduceTasks(4);

  // Output format "<Text> <IntWritable>"
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    return job.waitForCompletion(true) ? 0 : 1;
  }

    /** Mapper Logic
     *  The job of a mapper is to collect all the ratings of a song along with th song-id,
     *  and send <SongId - Rating> as <Key - Value> pair to the reducer, it will filter out the
     *  same rating given to a song.
     * */

  public static class Map extends Mapper<LongWritable, Text, Text, IntWritable> {

    // For Storing Just the Post-Id and corresponding Score.
    private HashMap<String, Integer> postList = new HashMap<>();

    // For Storing the top 10 posts by their score
    private HashMap<String, Integer> maxList = new HashMap<>();

    // Sorting helper variables
    int max = 0;
    String maxKey = null;

      public void map(LongWritable offset, Text lineText, Context context)
            throws IOException, InterruptedException {

        // Read each line from txt file and split with "tab"
        String data[] = lineText.toString().split(",");

        String songId = null;
        String rating = null;

        if(data.length > 2){

          // Extract the song id
          songId = data[1];

          // Extract rating from the data
          rating = data[3];

          // Send the <Album-Id, Rating> to the reducer
          context.write(new Text(songId), new IntWritable(Integer.parseInt(rating)));

        }

    }
  }

  /* Reducer Logic
  *  The job of a reducer is to find the average rating of a perticular song
  */

  public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable> {


      @Override
    public void reduce(Text word, Iterable<IntWritable> counts, Context context)
            throws IOException, InterruptedException {

          int sum  = 0;
          int counter = 0;

        for (IntWritable count : counts) {
            sum += count.get();
            counter += 1;
        }

        // Store <Album-Id, Avg-Rating>
        context.write(word, new IntWritable(sum/counter));
    }

  }
}
