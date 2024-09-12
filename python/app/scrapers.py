import json
from app.jobdata import JobData

from app.scraper_utils import extract_domain, fetch_html, extract_description, extract_json_data, USER_AGENT


# scrap nofluffjobs.pl
def scrap_nofluffjobs(url):
    headers = {
        'User-Agent': USER_AGENT
    }
    html = fetch_html(url, headers)
    json_data = extract_json_data(html, 'serverApp-state')

    posting_data = json_data.get('POSTING', {})
    company_name = posting_data.get('company', {}).get('name', "")
    requirements_must = [req['value'] for req in posting_data.get('requirements', {}).get('musts', [])]
    description_html = posting_data.get('requirements', {}).get('description', "")
    requirements_nice = extract_description(description_html)

    job_data = JobData()
    job_data.requirements_expected.extend(requirements_nice)
    job_data.technologies_expected.extend(requirements_must)
    job_data.company_name = company_name

    return json.dumps(job_data.to_dict(), indent=4, ensure_ascii=False)


# scrap pracuj.pl
def scrap_pracuj(url):
    headers = {
        'User-Agent': USER_AGENT
    }
    html = fetch_html(url, headers)
    json_data = extract_json_data(html, '__NEXT_DATA__')

    try:
        text_sections = (
            json_data
            .get('props', {})
            .get('pageProps', {})
            .get('dehydratedState', {})
            .get('queries', [])
        )

        job_data = JobData()

        for query in text_sections:
            state = query.get('state', {})
            data = state.get('data', {})
            sections = data.get('textSections', [])

            attributes = data.get('attributes', {})
            if attributes:
                job_data.company_name = attributes.get('displayEmployerName', job_data.company_name)

            for section in sections:
                section_type = section.get('sectionType', '')
                if section_type == 'technologies-expected':
                    job_data.technologies_expected.extend(section.get('textElements', []))
                elif section_type == 'requirements-expected':
                    job_data.requirements_expected.extend(section.get('textElements', []))

        return json.dumps(job_data.to_dict(), indent=4, ensure_ascii=False)

    except Exception as e:
        return f"Exception: {e}"


# scrap justjoin.it
# technologies_requirements must remain empty since justjoin.it
# keeps those data inside body with html elements
def scrap_justjoinit(url):
    headers = {
        'User-Agent': USER_AGENT
    }
    html = fetch_html(url, headers)
    json_data = extract_json_data(html, '__NEXT_DATA__')

    try:
        text_sections = (
            json_data
            .get('props', {})
            .get('pageProps', {})
            .get('offer', {})
        )

        job_data = JobData()
        job_data.company_name = text_sections.get('companyName', '')

        for technologies in text_sections.get('requiredSkills', []):
            job_data.technologies_expected.append(technologies.get('name', ''))

        return json.dumps(job_data.to_dict(), indent=4, ensure_ascii=False)

    except Exception as e:
        return f"Exception: {e}"


# scrap bulldogjob.pl
# technologies_requirements must remain empty since bulldogjob.pl
# keeps those data inside body with html elements
def scrap_bulldogjob(url):
    headers = {
        'User-Agent': USER_AGENT
    }
    html = fetch_html(url, headers)
    json_data = extract_json_data(html, '__NEXT_DATA__')

    try:
        text_sections = (
            json_data
            .get('props', {})
            .get('pageProps', {})
            .get('data', {})
            .get('job', {})
        )

        job_data = JobData()

        for technologies in text_sections.get('technologyTags', []):
            job_data.technologies_expected.append(technologies)

        company = text_sections.get('company', {})
        job_data.company_name = company.get('name', '')

        return json.dumps(job_data.to_dict(), indent=4, ensure_ascii=False)

    except Exception as e:
        return f"Exception: {e}"


def handle_scrapers(url):
    domain = extract_domain(url)
    match domain:
        case 'pracuj':
            return json.loads(scrap_pracuj(url))
        case 'nofluffjobs':
            return json.loads(scrap_nofluffjobs(url))
        case 'justjoin':
            return json.loads(scrap_justjoinit(url))
        case 'bulldogjob':
            return json.loads(scrap_bulldogjob(url))
        case _:
            return f"Scrape for {domain} not found."
