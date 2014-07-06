
def handle_exception(uri, exception) 
  $stderr.puts "\n\nERROR in #{$0} trying to access \"#{uri}\": #{exception.to_s}\n#{exception.backtrace.join("\n")}\n\n"
end

def init_http_object(uri)
  http  = Net::HTTP.new(uri.host, uri.port)
  if (uri.scheme == "https") 
    http.use_ssl     = true
    #http.cert        = OpenSSL::X509::Certificate.new(pem)
    #http.key         = OpenSSL::PKey::RSA.new(pem)
    http.verify_mode = OpenSSL::SSL::VERIFY_PEER
  end
  return http
end

def init_post_request(uri, config)
  request = Net::HTTP::Post.new(uri.request_uri)
  if (config[:http_basic_user]) 
      request.basic_auth(config[:http_basic_user], config[:http_basic_pass])
  end 
  token = config[:token]
  request.add_field 'Cookie', "lfrb-session-auth=#{token}"
  return request
end

def exec_post_request(uri, config)
  begin
    http = init_http_object uri
    request = init_post_request(uri, config)
    yield request
    response = http.request(request)
    puts "Response: #{response.code} #{response['Location']}"
    return response
  rescue OpenSSL::SSL::SSLError => exception
    handle_exception(uri.to_s, exception)
  rescue JSON::ParserError => exception
    handle_exception(uri.to_s, exception)
  rescue Exception => exception
    handle_exception(uri.to_s, exception)
  end
end

def handle_exception(uri, exception) 
  $stderr.puts "\n\nERROR in #{$0} trying to access \"#{uri}\": #{exception.to_s}\n#{exception.backtrace.join("\n")}\n\n"
end

