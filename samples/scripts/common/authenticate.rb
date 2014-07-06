
require_relative 'http_util'

class UserCtx 
  
  attr_reader :token
  
  def initialize(username, password) 
    @username = username
    @password = password
  end

  def authenticate(config)
    base_uri = config[:base_uri]
    uri = URI.parse("#{base_uri}/service/auth/tickets")
    puts "Authenticating against #{uri.to_s}"
  
    http  = init_http_object(uri)
    request = Net::HTTP::Post.new(uri.request_uri)
    if (config[:http_basic_user]) 
      request.basic_auth(config[:http_basic_user], config[:http_basic_pass])
    end        
    request.body = login_json
    request.add_field 'Content-Type', 'application/json'
    response = http.request(request)
    
    if (response.code.to_i != 201) 
      $stderr.puts "Authentication failed:\n#{response.body}"
      raise "Authentication failed (RC=#{response.code})." 
    end
    
    @token = response.body
  end
  
  def login_json
    JSON.generate ({:id => @username, :password => @password })
  end

end