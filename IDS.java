import java.util.*;
import java.io.File;  // Import the File class
import java.io.FileWriter;   // Import the FileWriter class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.io.IOException;  // Import the IOException class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files

public class IDS
{
  public static void main(String args[])
  {
    //declare filename and day number
    
    String eventFileName = args[0];
    String statsFileName = args[1];
    int days = Integer.parseInt(args[2]);
 
    //read initial input
    Input i1 = new Input();
    i1.readEvents(eventFileName);
    i1.readStats(statsFileName);
    i1.checkInconsistency();
    System.out.println("-----------------------------------");
    i1.printEvents();
    i1.printStats();

    //run simulation engine and log into files
    System.out.println("-----------------------------------");
    SimEngine se = new SimEngine(i1, days);

    //run analysis engine
    System.out.println("-----------------------------------");
    AnalysisEng ae = new AnalysisEng(i1);

    //run alert engine
    System.out.println("-----------------------------------");
    AlertEng ale = new AlertEng(i1);
  }
}

//Class to capture event parameters
class Event
{
  String event_name = "";
  String cd = "";
  int min_d = 0;
  int max_d = 0;
  double min_c = 0.0;
  double max_c = 0.0;
  int weight = 0;
}

//Class to capture event statistics
class Stat
{
  String event_name = "";
  double mean = 0.0;
  double sd = 0.0;
}

//Class to read and store events and stats
class Input
{
  int num_of_events = 0;
  int num_of_stats = 0;
  int threshold = 0;
  
  ArrayList<Event> events = new ArrayList<Event>();
  ArrayList<Stat> stats = new ArrayList<Stat>();

  ArrayList<String[]> arr = new ArrayList<String[]>();

  //read event parameter files
  public void readEvents(String filename)
  {
    arr.clear();
    
    //read events parameters
    try 
    {
      File f = new File(filename);
      Scanner sc = new Scanner(f);
      while (sc.hasNextLine()) {
        String data = sc.nextLine();
        String[] res = data.split(":");
        arr.add(res);
      }
      sc.close();
    } catch (FileNotFoundException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
    
    //get number of events in file
    this.num_of_events = Integer.parseInt(arr.get(0)[0]);

    //set event parameters for each event
    for (int i=1; i<=this.num_of_events; i++)
    {
      Event e = new Event();
      e.event_name = arr.get(i)[0];
      e.cd = arr.get(i)[1];
      e.weight = Integer.parseInt(arr.get(i)[4]);
      if(e.cd.toUpperCase().equals("D"))
      {
        if(arr.get(i)[2] != "")
        {
          e.min_d = Integer.parseInt(arr.get(i)[2]);
        }
        else
        {
          e.min_d = 0;
        }

        if(arr.get(i)[3] != "")
        {
          e.max_d = Integer.parseInt(arr.get(i)[3]);
        }
        else
        {
          e.max_d = 999;
        }
      }
      else
      {
        if(arr.get(i)[2] != "")
        {
          e.min_c = Double.parseDouble(arr.get(i)[2]);
        }
        else
        {
          e.min_c = 0.0;
        }

        if(arr.get(i)[3] != "")
        {
          e.max_c = Double.parseDouble(arr.get(i)[3]);
        }
        else
        {
          e.max_c = 999.0;
        }
      }

      events.add(e); 
    }

    //calculate threshold
    findThreshold();

  }

  //read statistics file
  public void readStats(String filename)
  {
    arr.clear();

    try 
    {
      File f = new File(filename);
      Scanner sc = new Scanner(f);
      while (sc.hasNextLine()) {
        String data = sc.nextLine();
        String[] res = data.split(":");
        arr.add(res);
      }
      sc.close();
    } catch (FileNotFoundException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
    
    this.num_of_stats = Integer.parseInt(arr.get(0)[0]);

    
    for (int i=1; i<=this.num_of_events; i++)
    {
      Stat s = new Stat();
      s.event_name = arr.get(i)[0];
      s.mean = Double.parseDouble(arr.get(i)[1]);
      s.sd = Double.parseDouble(arr.get(i)[2]);
      
      stats.add(s); 
    }
  }

  //function to print event parameters
  public void printEvents()
  {
    System.out.println("Number of events: " + num_of_events);
    for (int i=0; i<events.size(); i++)
    {
        Event e = events.get(i);

        if(e.cd.toUpperCase().equals("D"))
        {
          System.out.printf("%s %s %d %d %d \n", e.event_name, e.cd, e.min_d, e.max_d, e.weight);
        }
        else
        {
          System.out.printf("%s %s %.2f %.2f %d \n", e.event_name, e.cd, e.min_c, e.max_c, e.weight);
        }
    }
  }

  //function to print event statistics
  public void printStats()
  {
    
    System.out.println("\nNumber of stats: " + num_of_stats);
    for (int i=0; i<stats.size(); i++)
    {
        Stat s = stats.get(i);

        System.out.printf("%s %.2f %.2f \n", s.event_name, s.mean, s.sd);
    }
  }

  //check if events in parameter files are the same as statistics file
  public void checkInconsistency()
  {
    SortedSet<String> eventSet = new TreeSet<String>();
    SortedSet<String> statSet = new TreeSet<String>();

    for(int i=1; i<events.size(); i++)
    {
      eventSet.add(events.get(i).event_name.toUpperCase());
    }

    for(int i=1; i<stats.size(); i++)
    {
      statSet.add(stats.get(i).event_name.toUpperCase());
    }

    if(eventSet.equals(statSet))
    {
      System.out.println("No inconsistency detected");
    }
    else
    {
      System.out.println("Inconsistency detected");
    }
  }

  public void findThreshold()
  {
    int sum = 0;  
    for(Event e: events)
    {
      sum += e.weight;
    }
    threshold = sum * 2;
  }
}

//Simulation engine to simulate event over a number of days 
class SimEngine
{
  public SimEngine(Input input, int days)
  {

      for(Event e: input.events)
      {
        ArrayList<String> log = new ArrayList<String>();
        boolean isset = false;
        Stat temp_stat = new Stat();

        //Engine will check for events that have both event parameters and statistics before generation.
        for (Stat s: input.stats)
        {
          if(s.event_name.equals(e.event_name))
          {
            isset = true;
            temp_stat.event_name = s.event_name;
            temp_stat.mean = s.mean;
            temp_stat.sd = s.sd;
          }
        }
        
        //Values for each event type will be generated for the number of days before moving on to the next event type.
        for(int i=0; i<days; i++)
        {
          if(isset==true)
          {
            if(e.cd.toUpperCase().equals("D"))
            {
              String s = Generator.generateDiscreteEvent(e,temp_stat);
              s = s + "day " + (i+1);
              if(i!=days-1)
              {
                s += "\n";
              }
              log.add(s);
              //System.out.println(s);
            }
            else
            {
              String s = Generator.generateContinuousEvent(e,temp_stat);
              s = s + "day " + (i+1);
              if(i!=days-1)
              {
                s += "\n";
              }
              log.add(s);
              //System.out.println(s);
            }
          }
        }

        Logger.logEvent(e, log);
        System.out.println(e.event_name + " generated for " + days + "day(s)"); 
      }
  }
}

//class to generate discrete frequency and continuous value for the respective type of events
class Generator
{  
  static double factor = 2.5; //factor to tune the live data generation to get meaningful results for testing of alert engine
  public static String generateDiscreteEvent(Event event, Stat stat)
  {
    Random rand = new Random();
    int freq = (int) (rand.nextGaussian() * stat.sd * factor + stat.mean + 0.5);// add 0.5 for rounding off
    String temp = String.format("%s,%d,",event.event_name, freq);

    return temp;
  }

  public static String generateContinuousEvent(Event event, Stat stat)
  {
    Random rand = new Random();
    double val = rand.nextGaussian() * stat.sd * factor + stat.mean + 0.5;
    String temp = String.format("%s,%.2f,",event.event_name, val);

    return temp;
  }
}

//class to create log files
class Logger
{
  public static void logEvent(Event event, ArrayList<String> data)
  {
    String filename = event.event_name.concat(".txt");

    try {
      FileWriter myWriter = new FileWriter(filename);
      for(int i=0; i<data.size(); i++)
      {
        myWriter.write(data.get(i));
      }
      myWriter.close();
      //System.out.println("Successfully wrote to the file.");
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }
}

//Analysis Engine class
class AnalysisEng
{
  Input input = new Input();
  ArrayList<double[]> dataset = new ArrayList<double[]>();
  HashMap<String, Integer> eventNameMap = new HashMap<String, Integer>();

  public AnalysisEng(Input input)
  {
    this.input = input;
    int ctr = 0;

    System.out.println("Commencing analysis...\n");
    
    //read event data from file
    for (Event e: input.events)
    {
      String filename = e.event_name.concat(".txt");
      ArrayList<String> str_arr = readEventData(filename);
      double[] data_arr = new double[str_arr.size()];
      for (int j=0; j<str_arr.size(); j++)
      {
        String temp = str_arr.get(j);
        String[] res = temp.split(",");
        Double val = Double.parseDouble(res[1]);
        data_arr[j] = val;

        //System.out.println(val);
      }

      //find Event data statistics
      double mean = findMean(data_arr);
      double sd = findSD(data_arr, mean);

      String display = "";
      for(Stat s : input.stats)
      {
        if(e.event_name.equals(s.event_name))
        {
          display = String.format("Event name: %s\n Live data = mean: %.2f & sd: %.2f\n Stats = mean: %.2f. & sd: %.2f\n", e.event_name, mean, sd, s.mean, s.sd);
        }
      }

      System.out.println(display);

      //prepare data to write to file
      String statistics = String.format("mean,%.2f\nsd,%.2f", mean, sd);
      str_arr.add(statistics);
      filename = e.event_name.concat("_withStats.txt");

      //write data to log file
      writeEventData(filename, str_arr);

      //add data to a dataset for subseqent processing
      dataset.add(data_arr);
      eventNameMap.put(e.event_name, ctr++);
    }
  }

  //function to write read event data from log
  public ArrayList<String> readEventData (String filename)
  {
    ArrayList<String> arr = new ArrayList<String>();
    try 
    {
      File f = new File(filename);
      Scanner sc = new Scanner(f);
      while (sc.hasNextLine()) {
        String data = sc.nextLine();
        arr.add(data);
      }
      sc.close();
    } catch (FileNotFoundException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }

    return arr;
  }

  //function to write stats into log
  public void writeEventData (String filename, ArrayList<String> data)
  {
    try {
      FileWriter myWriter = new FileWriter(filename);
      for(int i=0; i<data.size(); i++)
      {
        String temp = data.get(i);
        if(i != data.size()-1)
        {
          temp += "\n";
        } 
        myWriter.write(temp);
      }
      myWriter.close();
      //System.out.println("Successfully wrote to the file.");
    } catch (IOException e) {
      System.out.println("An error occurred.");
      e.printStackTrace();
    }
  }

  //function to calculate mean
  public double findMean (double[] data_arr)
  {
      double sum = 0.0;
      double mean = 0.0;

      for(int i=0; i<data_arr.length; i++)
      {
        sum += data_arr[i];
      }

      mean = sum/data_arr.length;

      return mean;
  }

  //function to calculate standard deviation
  public double findSD (double[] data_arr, double mean)
  {
    double stdev = 0.0;

    for(double temp: data_arr) {
        stdev += Math.pow(temp - mean, 2);
    }
    stdev = Math.sqrt(stdev/data_arr.length);

    return stdev;
  }

  public int getIndex(String eventName)
  {
    return eventNameMap.get(eventName);
  }

}

//Alert Engine class
class AlertEng
{
  Input input = new Input();
  ArrayList<Integer> anomaly_list = new ArrayList<Integer>();
  int days = 0;

  public AlertEng(Input input)
  {
    this.input = input;
    boolean again = true;
    Scanner sc = new Scanner(System.in);  // Create a Scanner object
    
    System.out.println("Entering Alert Engine...\n");
    do{

      System.out.println("Enter name of new stats file:");
      String filename = sc.nextLine();  // Read filename

      System.out.println("Enter number of days:");
      days = Integer.parseInt(sc.nextLine());  // Read days

      input.readStats(filename);

      //run simulation engine and log into files
      System.out.println("-----------------------------------");
      SimEngine se = new SimEngine(input, days);

      //run analysis engine
      System.out.println("-----------------------------------");
      AnalysisEng ae = new AnalysisEng(input);

      findAnomaly(ae);

      System.out.println("\nDetecting anomaly...\n");
      checkAnomaly();

      System.out.println("Do you want to continue? [y/n]");
      String choice = sc.nextLine();
      if(choice.toUpperCase().equals("N"))
      {
        again = false;
      }

    }while(again);
  }

  public void findAnomaly(AnalysisEng ae)
  {
    anomaly_list.clear();
    for (int i=0; i<days; i++)
    {
      int anomaly_ctr = 0;
      
      for (Event e: input.events)
      {
        int event_index = ae.getIndex(e.event_name);
        double mean = 0.0;
        double sd = 0.0;
        int weight = e.weight;

        //get stats for the current event
        for(Stat s : input.stats)
        {
          if(e.event_name.equals(s.event_name))
          {
            mean = s.mean;
            sd = s.sd;
          }
        }

        double val = ae.dataset.get(event_index)[i];
        double num_of_sd = Math.abs(val - mean)/sd;
        int weightedSum = (int)(num_of_sd * weight);
        anomaly_ctr += weightedSum;
      }
      
      //System.out.println("anomaly counter for day " + (i+1) + ": " + anomaly_ctr);

      anomaly_list.add(anomaly_ctr);
    }
  }

  public void checkAnomaly()
  {
    boolean hasNoAnomaly = true;

    for (int i=0; i<anomaly_list.size(); i++)
    {
      int ac = anomaly_list.get(i);
      int th = input.threshold;
      
      if(ac > th)
      {
        System.out.println("ANOMALY DETECTED on day " + (i+1) + ". Anomaly counter: " + ac + " vs Threshold: " + th);
        hasNoAnomaly = false;
      }
    }
    if(hasNoAnomaly)
    {
      System.out.println("No anomaly detected");
    }
  }
}