require 'yaml'

def load_configuration
  script_dir = File.dirname(__FILE__)
  parent_dir = File.expand_path('..', script_dir)
  checks = ["#{script_dir}/config.yaml", "#{parent_dir}/config.yaml", "#{Dir.pwd}/config.yaml"]
  checks.each do |f| 
    if File.exists? f
      puts "Using configuration file #{f}"
      return YAML.load_file(f)
    end
  end
end

def get_configuration(name)
  allCfs = load_configuration
  if (!allCfs.nil? && allCfs.member?(name))
    allCfs[name]
  else
    {}
  end
end

def configure(options, environment = 'default')
  opts = options || {}
  cf = get_configuration(environment)
  cf.keys.each do |key|
    cf[(key.to_sym rescue key) || key] = cf.delete(key)
  end
  cf = cf.merge opts
  check_configuration cf
end

def check_configuration(config)
  unless (config[:base_uri])
    abort("No base URI of service given. Use -h option to dispay help.")
  end

  if (config[:user].nil? ^ config[:password].nil?)
    $stderr.puts "Please provide both a user name *and* password using --user and --password."
    exit 5
  end

  unless (config[:context])
    config[:context] = 'default'
  end
  config
end
